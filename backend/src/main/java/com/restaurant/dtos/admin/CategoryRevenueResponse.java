package com.restaurant.dtos.admin;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRevenueResponse {

    private String categoryName;
    private BigDecimal revenue;
    private Long ordersCount;
}
