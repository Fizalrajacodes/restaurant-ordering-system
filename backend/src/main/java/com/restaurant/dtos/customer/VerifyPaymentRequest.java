package com.restaurant.dtos.customer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPaymentRequest {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String orderNumber;
}
