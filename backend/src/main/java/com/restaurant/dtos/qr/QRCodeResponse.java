package com.restaurant.dtos.qr;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCodeResponse {

    private Long id;
    private Long tableId;
    private String tableNumber;
    private String tableName;
    private String token;
    private String qrData;
    private String imageUrl;
    private Boolean isActive;
    private Integer scanCount;
    private LocalDateTime lastScannedAt;
    private LocalDateTime createdAt;
}
