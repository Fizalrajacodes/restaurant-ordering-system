package com.restaurant.dtos.customer;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    private Long menuItemId;
    private String itemName;
    private String description;
    private BigDecimal unitPrice;
    private Integer quantity;
    private String foodType;
    private String imageUrl;
    private String specialInstructions;

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}