package com.restaurant.dtos.customer;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String tableNumber;
    private String tableName;
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
    private LocalDateTime estimatedReadyTime;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
}