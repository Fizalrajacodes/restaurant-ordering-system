package com.restaurant.dtos.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentOrderResponse {

    private String orderNumber;
    private String tableNumber;
    private String customerName;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
