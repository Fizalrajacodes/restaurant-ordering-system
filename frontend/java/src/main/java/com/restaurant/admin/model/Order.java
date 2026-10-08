package com.restaurant.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private Long id;
    private String orderNumber;
    private String tableNumber;
    private String tableName;
    private Integer tableCapacity;
    private String customerName;
    private String customerMobile;
    private String customerEmail;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String specialInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime servedAt;
    private LocalDateTime cancelledAt;
    private String cancelledReason;
    private List<OrderItem> items;
    private Payment payment;
    private List<OrderStatusHistory> statusHistory;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderItem {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String foodType;
    private String specialInstructions;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Payment {
    private Long id;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private BigDecimal amount;
    private String currency;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderStatusHistory {
    private Long id;
    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private String changeReason;
    private LocalDateTime changedAt;
}