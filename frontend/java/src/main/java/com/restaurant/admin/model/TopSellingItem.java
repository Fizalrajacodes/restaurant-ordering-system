package com.restaurant.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopSellingItem {
    private String itemName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}