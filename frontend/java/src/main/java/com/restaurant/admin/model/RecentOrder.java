package com.restaurant.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecentOrder {
    private String orderNumber;
    private String tableNumber;
    private String customerName;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
}