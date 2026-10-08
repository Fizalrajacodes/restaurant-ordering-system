package com.restaurant.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.restaurant.dtos.customer.CreatePaymentRequest;
import com.restaurant.dtos.customer.CreatePaymentResponse;
import com.restaurant.dtos.customer.VerifyPaymentRequest;
import com.restaurant.dtos.customer.VerifyPaymentResponse;
import com.restaurant.entity.Order;
import com.restaurant.entity.Payment;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.exception.ValidationException;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final WebSocketNotificationService notificationService;

    @Value("${razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        Order order = orderRepository.findByOrderNumber(request.getOrderNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderNumber()));

        if (order.getPaymentStatus() == Order.PaymentStatus.PAYMENT_COMPLETED) {
            throw new ValidationException("Payment already completed for this order");
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAYMENT_FAILED) {
            // Allow retry for failed payments
            paymentRepository.findByOrder(order).ifPresent(existing -> {
                existing.setPaymentStatus(Payment.PaymentStatus.CREATED);
                paymentRepository.save(existing);
            });
        }

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Amount in paise
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", order.getOrderNumber());
        orderRequest.put("payment_capture", 1); // Auto capture

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            com.razorpay.Order razorpayOrder = client.Orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            Payment payment = Payment.builder()
                    .order(order)
                    .razorpayOrderId(razorpayOrderId)
                    .amount(order.getTotalAmount())
                    .currency(currency)
                    .paymentStatus(Payment.PaymentStatus.CREATED)
                    .build();

            paymentRepository.save(payment);

            order.setPaymentStatus(Order.PaymentStatus.PAYMENT_PENDING);
            orderRepository.save(order);

            return CreatePaymentResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .razorpayKeyId(razorpayKeyId)
                    .amount(order.getTotalAmount())
                    .currency(currency)
                    .orderNumber(order.getOrderNumber())
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for order: {}", request.getOrderNumber(), e);
            throw new ValidationException("Failed to create payment: " + e.getMessage());
        }
    }

    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying payment for order: {}", request.getOrderNumber());

        Order order = orderRepository.findByOrderNumber(request.getOrderNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderNumber()));

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for Razorpay order: " + request.getRazorpayOrderId()));

        if (!verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            log.error("Payment signature verification failed for order: {}", request.getOrderNumber());
            
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);

            order.setPaymentStatus(Order.PaymentStatus.PAYMENT_FAILED);
            orderRepository.save(order);

            return VerifyPaymentResponse.builder()
                    .success(false)
                    .message("Payment verification failed: Invalid signature")
                    .orderNumber(order.getOrderNumber())
                    .orderStatus(order.getOrderStatus().name())
                    .paymentStatus(order.getPaymentStatus().name())
                    .build();
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentStatus(Payment.PaymentStatus.PAID);
        payment.setPaymentMethod("RAZORPAY");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setPaymentStatus(Order.PaymentStatus.PAYMENT_COMPLETED);
        order.setOrderStatus(Order.OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Payment verified successfully for order: {}", order.getOrderNumber());

        notificationService.sendNewOrderNotification(order);

        return VerifyPaymentResponse.builder()
                .success(true)
                .message("Payment verified successfully")
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .build();
    }

    private boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String expectedSignature = hmacSha256Hex(razorpayKeySecret, payload);
            return expectedSignature.equals(razorpaySignature);
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }

    private String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error computing HMAC signature", e);
            throw new ValidationException("Failed to compute signature");
        }
    }

    @Transactional
    public void handlePaymentWebhook(String payload, String signature) {
        log.info("Received Razorpay webhook");
        
        if (!verifyWebhookSignature(payload, signature)) {
            log.error("Webhook signature verification failed");
            throw new ValidationException("Invalid webhook signature");
        }

        try {
            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");
            
            if ("payment.captured".equals(event) || "payment.failed".equals(event)) {
                JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String razorpayOrderId = paymentEntity.getString("order_id");
                String razorpayPaymentId = paymentEntity.getString("id");
                String status = paymentEntity.getString("status");

                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for webhook"));

                Order order = payment.getOrder();

                if ("captured".equals(status)) {
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    payment.setPaymentStatus(Payment.PaymentStatus.PAID);
                    payment.setPaymentMethod("RAZORPAY");
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    order.setPaymentStatus(Order.PaymentStatus.PAYMENT_COMPLETED);
                    order.setOrderStatus(Order.OrderStatus.CONFIRMED);
                    order.setConfirmedAt(LocalDateTime.now());
                    orderRepository.save(order);

                    notificationService.sendNewOrderNotification(order);
                } else if ("failed".equals(status)) {
                    payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                    payment.setFailureReason(paymentEntity.optString("error_description", "Payment failed"));
                    paymentRepository.save(payment);

                    order.setPaymentStatus(Order.PaymentStatus.PAYMENT_FAILED);
                    orderRepository.save(order);
                }
            }
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new ValidationException("Failed to process webhook: " + e.getMessage());
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String expectedSignature = hmacSha256Hex(
                    System.getenv().getOrDefault("RAZORPAY_WEBHOOK_SECRET", razorpayKeySecret), payload);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }
}