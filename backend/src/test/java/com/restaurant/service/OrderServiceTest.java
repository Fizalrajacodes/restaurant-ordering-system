package com.restaurant.service;

import com.restaurant.dtos.admin.OrderStatusUpdateRequest;
import com.restaurant.entity.*;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private WebSocketNotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrderByNumber_ShouldReturnOrder() {
        // Arrange
        Order order = createTestOrder();
        when(orderRepository.findByOrderNumber("ORD-20240101-ABCD")).thenReturn(Optional.of(order));

        // Act
        var result = orderService.getOrderByNumber("ORD-20240101-ABCD");

        // Assert
        assertNotNull(result);
        assertEquals("ORD-20240101-ABCD", result.getOrderNumber());
        verify(orderRepository, times(1)).findByOrderNumber("ORD-20240101-ABCD");
    }

    @Test
    void getOrderByNumber_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findByOrderNumber("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> orderService.getOrderByNumber("INVALID"));
    }

    @Test
    void updateOrderStatus_ShouldUpdateSuccessfully() {
        // Arrange
        Order order = createTestOrder();
        order.setOrderStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        var result = orderService.updateOrderStatus(1L, 
            new OrderStatusUpdateRequest(Order.OrderStatus.CONFIRMED, "Admin", "Test"));

        // Assert
        assertEquals(Order.OrderStatus.CONFIRMED.name(), result.getOrderStatus());
        verify(orderRepository, times(1)).save(order);
        verify(statusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    private Order createTestOrder() {
        RestaurantTable table = RestaurantTable.builder()
                .id(1L)
                .tableNumber("TABLE-01")
                .tableName("Table 01")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        return Order.builder()
                .id(1L)
                .orderNumber("ORD-20240101-ABCD")
                .table(table)
                .customerName("John Doe")
                .customerMobile("9876543210")
                .subtotal(new BigDecimal("560.00"))
                .tax(new BigDecimal("28.00"))
                .discount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("588.00"))
                .orderStatus(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private OrderStatusUpdateRequest createStatusRequest(Order.OrderStatus status) {
        return new OrderStatusUpdateRequest(status, "Admin", "Test update");
    }
}