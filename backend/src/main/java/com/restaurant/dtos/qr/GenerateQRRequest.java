package com.restaurant.dtos.qr;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQRRequest {

    private Long tableId;
    private String baseUrl;
}
