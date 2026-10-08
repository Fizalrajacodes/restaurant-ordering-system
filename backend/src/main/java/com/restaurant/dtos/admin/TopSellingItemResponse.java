package com.restaurant.dtos.admin;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingItemResponse {

    private String itemName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}
