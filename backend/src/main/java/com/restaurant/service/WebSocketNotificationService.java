package com.restaurant.service;

import com.restaurant.dtos.websocket.KitchenNotification;
import com.restaurant.dtos.websocket.OrderNotification;
import com.restaurant.dtos.websocket.OrderStatusUpdateNotification;
import com.restaurant.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNewOrderNotification(Order order) {
        OrderNotification notification = OrderNotification.builder()
                .type("NEW_ORDER")
                .message("New order received from " + order.getTable().getTableName())
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .customerName(order.getCustomerName())
                .totalAmount(order.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/orders/new", notification);
        messagingTemplate.convertAndSend("/topic/kitchen/new", buildKitchenNotification(order));
        log.info("Sent new order notification for order: {}", order.getOrderNumber());
    }

    public void sendOrderStatusUpdate(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        OrderStatusUpdateNotification notification = OrderStatusUpdateNotification.builder()
                .type("ORDER_STATUS_UPDATE")
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .updatedBy("STAFF")
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/orders/status", notification);
        
        if (newStatus == Order.OrderStatus.READY) {
            OrderNotification readyNotification = OrderNotification.builder()
                    .type("ORDER_READY")
                    .message("Order " + order.getOrderNumber() + " is ready for pickup")
                    .orderNumber(order.getOrderNumber())
                    .tableNumber(order.getTable().getTableNumber())
                    .customerName(order.getCustomerName())
                    .totalAmount(order.getTotalAmount())
                    .timestamp(LocalDateTime.now())
                    .build();
            messagingTemplate.convertAndSend("/topic/orders/ready", readyNotification);
        }
        
        log.info("Sent order status update for order: {} from {} to {}", order.getOrderNumber(), oldStatus, newStatus);
    }

    public void sendOrderCancelledNotification(Order order) {
        OrderNotification notification = OrderNotification.builder()
                .type("ORDER_CANCELLED")
                .message("Order " + order.getOrderNumber() + " has been cancelled")
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .customerName(order.getCustomerName())
                .totalAmount(order.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/orders/cancelled", notification);
        log.info("Sent order cancelled notification for order: {}", order.getOrderNumber());
    }

    private KitchenNotification buildKitchenNotification(Order order) {
        return KitchenNotification.builder()
                .type("KITCHEN_NEW_ORDER")
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .customerName(order.getCustomerName())
                .itemsCount(order.getOrderItems().size())
                .totalAmount(order.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .build();
    }
}