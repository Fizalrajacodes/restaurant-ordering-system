package com.restaurant.dtos.websocket;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateNotification {

    private String type;
    private String orderNumber;
    private String tableNumber;
    private String oldStatus;
    private String newStatus;
    private String updatedBy;
    private LocalDateTime timestamp;
}
