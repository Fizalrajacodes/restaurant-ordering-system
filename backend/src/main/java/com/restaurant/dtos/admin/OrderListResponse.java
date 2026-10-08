package com.restaurant.dtos.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponse {

    private Long id;
    private String orderNumber;
    private String tableNumber;
    private String tableName;
    private String customerName;
    private String customerMobile;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer itemsCount;
}
