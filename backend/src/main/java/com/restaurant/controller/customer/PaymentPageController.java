package com.restaurant.controller.customer;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.customer.CreatePaymentRequest;
import com.restaurant.dtos.customer.CreatePaymentResponse;
import com.restaurant.dtos.customer.OrderResponse;
import com.restaurant.entity.Order;
import com.restaurant.repository.OrderRepository;
import com.restaurant.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PaymentPageController {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam("order") String orderNumber,
                                   @RequestParam("table") String tableToken,
                                   Model model) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        CreatePaymentResponse paymentResponse;
        try {
            paymentResponse = paymentService.createPayment(
                    CreatePaymentRequest.builder().orderNumber(orderNumber).build());
        } catch (Exception e) {
            // Demo/fallback mode when Razorpay keys are invalid
            paymentResponse = CreatePaymentResponse.builder()
                    .razorpayKeyId("rzp_test_demo")
                    .razorpayOrderId("order_demo_" + System.currentTimeMillis())
                    .amount(order.getTotalAmount())
                    .currency("INR")
                    .build();
        }

        model.addAttribute("razorpayKeyId", paymentResponse.getRazorpayKeyId());
        model.addAttribute("razorpayOrderId", paymentResponse.getRazorpayOrderId());
        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("amount", paymentResponse.getAmount());
        model.addAttribute("tableToken", tableToken);
        model.addAttribute("restaurantName", "Smart Restaurant");
        model.addAttribute("restaurantLogo", "/images/logo.png");

        return "customer/payment";
    }
}