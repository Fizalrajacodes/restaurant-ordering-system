package com.restaurant.admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusUpdateRequest {
    private String newStatus;
    private String changedBy;
    private String changeReason;
}