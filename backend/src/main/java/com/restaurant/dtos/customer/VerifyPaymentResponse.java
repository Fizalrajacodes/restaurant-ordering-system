package com.restaurant.dtos.customer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPaymentResponse {

    private boolean success;
    private String message;
    private String orderNumber;
    private String orderStatus;
    private String paymentStatus;
}
