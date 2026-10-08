package com.restaurant.dtos.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryResponse {

    private Long id;
    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private String changeReason;
    private LocalDateTime changedAt;
}
