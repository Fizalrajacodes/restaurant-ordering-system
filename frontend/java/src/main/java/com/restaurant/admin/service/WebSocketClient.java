package com.restaurant.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.admin.AdminApplication;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClient.class);
    
    private final AdminApplication app;
    private final ObjectMapper objectMapper;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private boolean isConnected = false;
    
    public WebSocketClient(AdminApplication app) {
        this.app = app;
        this.objectMapper = new ObjectMapper();
    }
    
    public void connect() {
        if (isConnected) {
            log.info("WebSocket already connected");
            return;
        }
        
        try {
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            
            SockJsClient sockJsClient = new SockJsClient(transports);
            stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            
            String wsUrl = "http://localhost:8080/ws";
            StompSessionHandlerAdapter handler = new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    log.info("WebSocket connected successfully");
                    isConnected = true;
                    stompSession = session;
                    subscribeToTopics();
                }
                
                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    log.error("WebSocket error", exception);
                }
                
                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    log.error("WebSocket transport error", exception);
                    isConnected = false;
                }
            };
            
            ListenableFuture<StompSession> springFuture = stompClient.connect(wsUrl, handler);
            CompletableFuture<StompSession> future = new CompletableFuture<>();
            springFuture.addCallback(new ListenableFutureCallback<StompSession>() {
                @Override
                public void onSuccess(StompSession result) {
                    future.complete(result);
                }
                @Override
                public void onFailure(Throwable ex) {
                    future.completeExceptionally(ex);
                }
            });
            future.get(); // Wait for connection
            
        } catch (Exception e) {
            log.error("Failed to connect WebSocket", e);
            isConnected = false;
        }
    }
    
    private void subscribeToTopics() {
        if (stompSession == null || !stompSession.isConnected()) {
            return;
        }
        
        // Subscribe to new orders
        stompSession.subscribe("/topic/orders/new", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("New order received: {}", payload);
                showNewOrderNotification(payload);
            }
        });
        
        // Subscribe to order status updates
        stompSession.subscribe("/topic/orders/status", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("Order status update: {}", payload);
                showStatusUpdateNotification(payload);
            }
        });
        
        // Subscribe to kitchen notifications
        stompSession.subscribe("/topic/kitchen/new", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("Kitchen new order: {}", payload);
                showKitchenNotification(payload);
            }
        });
    }
    
    private void showNewOrderNotification(Object payload) {
        Platform.runLater(() -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                var node = objectMapper.readTree(json);
                String message = node.get("message").asText();
                String orderNumber = node.get("orderNumber").asText();
                String tableNumber = node.get("tableNumber").asText();
                String amount = node.get("totalAmount").asText();
                
                showNotification("🔔 NEW ORDER", 
                    "Order " + orderNumber + "\nTable " + tableNumber + "\nAmount: ₹" + amount);
            } catch (Exception e) {
                log.error("Failed to parse new order notification", e);
            }
        });
    }
    
    private void showStatusUpdateNotification(Object payload) {
        Platform.runLater(() -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                var node = objectMapper.readTree(json);
                String orderNumber = node.get("orderNumber").asText();
                String tableNumber = node.get("tableNumber").asText();
                String newStatus = node.get("newStatus").asText();
                
                showNotification("📊 ORDER UPDATE", 
                    "Order " + orderNumber + "\nTable " + tableNumber + "\nStatus: " + newStatus);
            } catch (Exception e) {
                log.error("Failed to parse status update notification", e);
            }
        });
    }
    
    private void showKitchenNotification(Object payload) {
        Platform.runLater(() -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                var node = objectMapper.readTree(json);
                String message = node.get("message").asText();
                
                showNotification("🍳 KITCHEN", message);
            } catch (Exception e) {
                log.error("Failed to parse kitchen notification", e);
            }
        });
    }
    
    private void showNotification(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
    
    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            try {
                stompSession.disconnect();
            } catch (Exception e) {
                log.error("Error disconnecting WebSocket", e);
            }
        }
        if (stompClient != null) {
            stompClient.stop();
        }
        isConnected = false;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}