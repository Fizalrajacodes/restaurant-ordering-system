package com.restaurant.dtos.admin;

import com.restaurant.entity.Order;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {

    private Order.OrderStatus newStatus;
    private String changedBy;
    private String changeReason;
}
