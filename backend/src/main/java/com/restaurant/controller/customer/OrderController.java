package com.restaurant.controller.customer;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.customer.CreateOrderRequest;
import com.restaurant.dtos.customer.OrderResponse;
import com.restaurant.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("customerOrderController")
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ApiResponse.success("Order created successfully", response);
    }

    @GetMapping("/{orderNumber}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable String orderNumber) {
        return ApiResponse.success(orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/track/{orderNumber}")
    public ApiResponse<OrderResponse> trackOrder(
            @PathVariable String orderNumber,
            @RequestParam String mobile) {
        return ApiResponse.success(orderService.getOrderByNumberAndMobile(orderNumber, mobile));
    }

    @GetMapping("/customer/{mobile}")
    public ApiResponse<List<OrderResponse>> getCustomerOrders(@PathVariable String mobile) {
        return ApiResponse.success(orderService.getCustomerOrders(mobile));
    }
}