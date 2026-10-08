package com.restaurant.dtos.websocket;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotification {

    private String type;
    private String message;
    private String orderNumber;
    private String tableNumber;
    private String customerName;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}
