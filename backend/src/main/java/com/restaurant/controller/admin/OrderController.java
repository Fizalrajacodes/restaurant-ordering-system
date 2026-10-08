package com.restaurant.controller.admin;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.admin.OrderDetailResponse;
import com.restaurant.dtos.admin.OrderListResponse;
import com.restaurant.dtos.admin.OrderStatusUpdateRequest;
import com.restaurant.dtos.PageResponse;
import com.restaurant.entity.Order;
import com.restaurant.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminOrderController")
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<Page<OrderListResponse>> getOrders(
            @RequestParam(required = false) List<Order.OrderStatus> status,
            Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return ApiResponse.success(orderService.getOrdersByStatus(status, pageable));
        }
        return ApiResponse.success(orderService.getOrders(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderDetail(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ApiResponse<OrderDetailResponse> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDetailResponse response = orderService.getOrderDetail(
                orderService.getOrderByNumber(orderNumber).getId());
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderDetailResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ApiResponse.success("Order status updated successfully",
                orderService.updateOrderStatus(id, request));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<OrderListResponse>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        List<OrderListResponse> orders = orderService.getOrdersByStatus(List.of(status),
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        return ApiResponse.success(orders);
    }
}