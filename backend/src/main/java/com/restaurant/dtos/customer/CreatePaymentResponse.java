package com.restaurant.dtos.customer;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentResponse {

    private String razorpayOrderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private String orderNumber;
}
