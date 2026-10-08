package com.restaurant.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantTable {
    private Long id;
    private String tableNumber;
    private String tableName;
    private Integer capacity;
    private String status;
    private String qrToken;
    private String qrCodeUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long activeOrdersCount;
}