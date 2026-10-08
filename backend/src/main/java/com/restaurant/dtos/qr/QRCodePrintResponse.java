package com.restaurant.dtos.qr;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCodePrintResponse {

    private String tableNumber;
    private String tableName;
    private String qrCodeImageBase64;
    private String qrDataUrl;
}
