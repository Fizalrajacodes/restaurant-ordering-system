package com.restaurant.service;

import com.restaurant.dtos.customer.CartDTO;
import com.restaurant.dtos.customer.CartItemDTO;
import com.restaurant.dtos.customer.CreateOrderRequest;
import com.restaurant.dtos.customer.OrderItemResponse;
import com.restaurant.dtos.customer.OrderResponse;
import com.restaurant.dtos.customer.PaymentResponse;
import com.restaurant.dtos.admin.OrderDetailResponse;
import com.restaurant.dtos.admin.OrderItemAdminResponse;
import com.restaurant.dtos.admin.OrderListResponse;
import com.restaurant.dtos.admin.OrderStatusHistoryResponse;
import com.restaurant.dtos.admin.OrderStatusUpdateRequest;
import com.restaurant.dtos.admin.PaymentAdminResponse;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.entity.OrderStatusHistory;
import com.restaurant.entity.Payment;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.exception.ValidationException;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.OrderStatusHistoryRepository;
import com.restaurant.repository.PaymentRepository;
import com.restaurant.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantTableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final WebSocketNotificationService notificationService;

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(5.0);

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        RestaurantTable table = tableRepository.findByTableNumber(request.getTableNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + request.getTableNumber()));

        if (!table.getIsActive()) {
            throw new ValidationException("Table is not active: " + request.getTableNumber());
        }

        Order order = buildOrder(request, table);
        order = orderRepository.save(order);

        List<OrderItem> orderItems = buildOrderItems(order, request.getItems());
        orderItemRepository.saveAll(orderItems);
        order.setOrderItems(orderItems);

        recalculateOrderTotals(order);
        order = orderRepository.save(order);

        createStatusHistory(order, null, Order.OrderStatus.PENDING, "SYSTEM", "Order created");

        return mapToOrderResponse(order);
    }

    private Order buildOrder(CreateOrderRequest request, RestaurantTable table) {
        String orderNumber = generateOrderNumber();
        
        BigDecimal subtotal = request.getItems().stream()
                .map(item -> {
                    MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + item.getMenuItemId()));
                    return menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(TAX_RATE).divide(BigDecimal.valueOf(100));
        BigDecimal totalAmount = subtotal.add(tax);

        return Order.builder()
                .orderNumber(orderNumber)
                .table(table)
                .customerName(request.getCustomerName())
                .customerMobile(request.getCustomerMobile())
                .customerEmail(request.getCustomerEmail())
                .subtotal(subtotal)
                .tax(tax)
                .discount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .orderStatus(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PAYMENT_PENDING)
                .specialInstructions(request.getSpecialInstructions())
                .build();
    }

    private List<OrderItem> buildOrderItems(Order order, List<CreateOrderRequest.CreateOrderItemRequest> items) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CreateOrderRequest.CreateOrderItemRequest itemRequest : items) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemRequest.getMenuItemId()));

            if (!menuItem.getIsAvailable()) {
                throw new ValidationException("Menu item not available: " + menuItem.getName());
            }

            BigDecimal totalPrice = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .itemName(menuItem.getName())
                    .itemDescription(menuItem.getDescription())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .totalPrice(totalPrice)
                    .foodType(menuItem.getFoodType().name())
                    .specialInstructions(itemRequest.getSpecialInstructions())
                    .build();

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private void recalculateOrderTotals(Order order) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal tax = subtotal.multiply(TAX_RATE).divide(BigDecimal.valueOf(100));
        BigDecimal totalAmount = subtotal.add(tax).subtract(order.getDiscount());

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTotalAmount(totalAmount);
    }

    private String generateOrderNumber() {
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("ORD-%s-%s", datePart, randomPart);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return mapToOrderResponse(order);
    }

    public OrderResponse getOrderByNumberAndMobile(String orderNumber, String mobile) {
        Order order = orderRepository.findByOrderNumberAndCustomerMobile(orderNumber, mobile)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for given details"));
        return mapToOrderResponse(order);
    }

    public Page<OrderListResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToOrderListResponse);
    }

    public Page<OrderListResponse> getOrdersByStatus(List<Order.OrderStatus> statuses, Pageable pageable) {
        return orderRepository.findByOrderStatusIn(statuses, pageable).map(this::mapToOrderListResponse);
    }

    public OrderDetailResponse getOrderDetail(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderDetailResponse(order);
    }

    @Transactional
    public OrderDetailResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        Order.OrderStatus oldStatus = order.getOrderStatus();
        Order.OrderStatus newStatus = request.getNewStatus();

        if (oldStatus == newStatus) {
            return mapToOrderDetailResponse(order);
        }

        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new ValidationException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }

        order.setOrderStatus(newStatus);
        updateStatusTimestamps(order, newStatus);
        order = orderRepository.save(order);

        createStatusHistory(order, oldStatus, newStatus, request.getChangedBy(), request.getChangeReason());

        notificationService.sendOrderStatusUpdate(order, oldStatus, newStatus);

        return mapToOrderDetailResponse(order);
    }

    private boolean isValidStatusTransition(Order.OrderStatus from, Order.OrderStatus to) {
        if (from == Order.OrderStatus.CANCELLED || from == Order.OrderStatus.SERVED) {
            return false;
        }
        
        return switch (from) {
            case PENDING -> to == Order.OrderStatus.CONFIRMED || to == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> to == Order.OrderStatus.PREPARING || to == Order.OrderStatus.CANCELLED;
            case PREPARING -> to == Order.OrderStatus.READY || to == Order.OrderStatus.CANCELLED;
            case READY -> to == Order.OrderStatus.SERVED || to == Order.OrderStatus.CANCELLED;
            case SERVED, CANCELLED -> false;
        };
    }

    private void updateStatusTimestamps(Order order, Order.OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case CONFIRMED -> order.setConfirmedAt(now);
            case PREPARING -> order.setPreparingAt(now);
            case READY -> order.setReadyAt(now);
            case SERVED -> order.setServedAt(now);
            case CANCELLED -> {
                order.setCancelledAt(now);
                order.setCancelledReason("Cancelled by staff");
            }
        }
    }

    private void createStatusHistory(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus, 
                                     String changedBy, String reason) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy != null ? changedBy : "SYSTEM")
                .changeReason(reason)
                .build();
        statusHistoryRepository.save(history);
    }

    public List<OrderResponse> getCustomerOrders(String mobile) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return orderRepository.findByCustomerMobileAndCreatedAtBetween(mobile, thirtyDaysAgo, LocalDateTime.now())
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .tableName(order.getTable().getTableName())
                .customerName(order.getCustomerName())
                .customerMobile(order.getCustomerMobile())
                .customerEmail(order.getCustomerEmail())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .specialInstructions(order.getSpecialInstructions())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .estimatedReadyTime(order.getEstimatedReadyTime())
                .items(order.getOrderItems().stream().map(this::mapToOrderItemResponse).collect(Collectors.toList()))
                .build();

        if (order.getPayments() != null && !order.getPayments().isEmpty()) {
            response.setPayment(mapToPaymentResponse(order.getPayments().get(0)));
        }

        return response;
    }

    private OrderListResponse mapToOrderListResponse(Order order) {
        return OrderListResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .tableName(order.getTable().getTableName())
                .customerName(order.getCustomerName())
                .customerMobile(order.getCustomerMobile())
                .customerEmail(order.getCustomerEmail())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .itemsCount(order.getOrderItems().size())
                .build();
    }

    private OrderDetailResponse mapToOrderDetailResponse(Order order) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .tableName(order.getTable().getTableName())
                .tableCapacity(order.getTable().getCapacity())
                .customerName(order.getCustomerName())
                .customerMobile(order.getCustomerMobile())
                .customerEmail(order.getCustomerEmail())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .specialInstructions(order.getSpecialInstructions())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .preparingAt(order.getPreparingAt())
                .readyAt(order.getReadyAt())
                .servedAt(order.getServedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelledReason(order.getCancelledReason())
                .items(order.getOrderItems().stream().map(this::mapToOrderItemAdminResponse).collect(Collectors.toList()))
                .payment(order.getPayments().isEmpty() ? null : mapToPaymentAdminResponse(order.getPayments().get(0)))
                .statusHistory(order.getStatusHistory().stream().map(this::mapToStatusHistoryResponse).collect(Collectors.toList()))
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .itemName(item.getItemName())
                .description(item.getItemDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .foodType(item.getFoodType())
                .specialInstructions(item.getSpecialInstructions())
                .build();
    }

    private OrderItemAdminResponse mapToOrderItemAdminResponse(OrderItem item) {
        return OrderItemAdminResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .itemName(item.getItemName())
                .description(item.getItemDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .foodType(item.getFoodType())
                .specialInstructions(item.getSpecialInstructions())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private PaymentAdminResponse mapToPaymentAdminResponse(Payment payment) {
        return PaymentAdminResponse.builder()
                .id(payment.getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private OrderStatusHistoryResponse mapToStatusHistoryResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponse.builder()
                .id(history.getId())
                .oldStatus(history.getOldStatus() != null ? history.getOldStatus().name() : null)
                .newStatus(history.getNewStatus().name())
                .changedBy(history.getChangedBy())
                .changeReason(history.getChangeReason())
                .changedAt(history.getChangedAt())
                .build();
    }
}