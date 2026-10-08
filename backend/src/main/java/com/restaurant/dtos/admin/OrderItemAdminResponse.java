package com.restaurant.dtos.admin;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemAdminResponse {

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
