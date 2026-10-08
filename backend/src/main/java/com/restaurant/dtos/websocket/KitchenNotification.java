package com.restaurant.dtos.websocket;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenNotification {

    private String type;
    private String orderNumber;
    private String tableNumber;
    private String customerName;
    private Integer itemsCount;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}
