package com.restaurant.controller.customer;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.customer.CreatePaymentRequest;
import com.restaurant.dtos.customer.CreatePaymentResponse;
import com.restaurant.dtos.customer.VerifyPaymentRequest;
import com.restaurant.dtos.customer.VerifyPaymentResponse;
import com.restaurant.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ApiResponse<CreatePaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ApiResponse.success(paymentService.createPayment(request));
    }

    @PostMapping("/verify")
    public ApiResponse<VerifyPaymentResponse> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        return ApiResponse.success(paymentService.verifyPayment(request));
    }

    @PostMapping("/webhook")
    public ApiResponse<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handlePaymentWebhook(payload, signature);
        return ApiResponse.success(null);
    }
}