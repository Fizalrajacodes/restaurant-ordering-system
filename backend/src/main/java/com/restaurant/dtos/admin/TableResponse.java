package com.restaurant.dtos.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableResponse {

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
