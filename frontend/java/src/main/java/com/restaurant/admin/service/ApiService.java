package com.restaurant.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.restaurant.admin.model.*;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.hc.core5.http.ParseException;

public class ApiService {
    private static final Logger log = LoggerFactory.getLogger(ApiService.class);
    
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private String authToken;

    public ApiService(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public void clearAuthToken() {
        this.authToken = null;
    }

    // Authentication
    public CompletableFuture<AuthResponse> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost request = new HttpPost(baseUrl + "/api/auth/login");
                request.setHeader("Content-Type", "application/json");
                
                String json = objectMapper.writeValueAsString(new LoginRequest(email, password));
                request.setEntity(EntityBuilder.create()
                        .setText(json)
                        .setContentType(ContentType.APPLICATION_JSON)
                        .build());

                return executeRequest(request, AuthResponse.class);
            } catch (Exception e) {
                log.error("Login failed", e);
                throw new RuntimeException("Login failed: " + e.getMessage());
            }
        });
    }

    // Dashboard
    public CompletableFuture<Dashboard> getDashboard() {
        return getAsync("/api/admin/dashboard", Dashboard.class);
    }

    // Orders
    public CompletableFuture<PageResponse<Order>> getOrders(String status, int page, int size) {
        StringBuilder url = new StringBuilder("/api/admin/orders?page=" + page + "&size=" + size);
        if (status != null && !status.isEmpty()) {
            url.append("&status=").append(status);
        }
        return getAsync(url.toString(), new TypeReference<PageResponse<Order>>() {});
    }

    public CompletableFuture<Order> getOrder(Long id) {
        return getAsync("/api/admin/orders/" + id, Order.class);
    }

    public CompletableFuture<Order> getOrderByNumber(String orderNumber) {
        return getAsync("/api/admin/orders/number/" + orderNumber, Order.class);
    }

    public CompletableFuture<Order> updateOrderStatus(Long id, String newStatus, String changedBy, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPut request = new HttpPut(baseUrl + "/api/admin/orders/" + id + "/status");
                addAuthHeader(request);
                request.setHeader("Content-Type", "application/json");
                
                String json = objectMapper.writeValueAsString(new OrderStatusUpdateRequest(newStatus, changedBy, reason));
                request.setEntity(EntityBuilder.create()
                        .setText(json)
                        .setContentType(ContentType.APPLICATION_JSON)
                        .build());

                return executeRequest(request, Order.class);
            } catch (Exception e) {
                log.error("Failed to update order status", e);
                throw new RuntimeException("Failed to update order status: " + e.getMessage());
            }
        });
    }

    // Menu Items
    public CompletableFuture<List<MenuItem>> getMenuItems() {
        return getAsync("/api/admin/menu-items", new TypeReference<List<MenuItem>>() {});
    }

    public CompletableFuture<MenuItem> createMenuItem(MenuItemRequest request) {
        return postAsync("/api/admin/menu-items", request, MenuItem.class);
    }

    public CompletableFuture<MenuItem> updateMenuItem(Long id, MenuItemRequest request) {
        return putAsync("/api/admin/menu-items/" + id, request, MenuItem.class);
    }

    public CompletableFuture<Void> deleteMenuItem(Long id) {
        return deleteAsync("/api/admin/menu-items/" + id);
    }

    // Categories
    public CompletableFuture<List<Category>> getCategories() {
        return getAsync("/api/admin/categories", new TypeReference<List<Category>>() {});
    }

    public CompletableFuture<Category> createCategory(CategoryRequest request) {
        return postAsync("/api/admin/categories", request, Category.class);
    }

    public CompletableFuture<Category> updateCategory(Long id, CategoryRequest request) {
        return putAsync("/api/admin/categories/" + id, request, Category.class);
    }

    public CompletableFuture<Void> deleteCategory(Long id) {
        return deleteAsync("/api/admin/categories/" + id);
    }

    // Tables
    public CompletableFuture<List<RestaurantTable>> getTables() {
        return getAsync("/api/admin/tables", new TypeReference<List<RestaurantTable>>() {});
    }

    public CompletableFuture<RestaurantTable> createTable(TableRequest request) {
        return postAsync("/api/admin/tables", request, RestaurantTable.class);
    }

    public CompletableFuture<RestaurantTable> updateTable(Long id, TableRequest request) {
        return putAsync("/api/admin/tables/" + id, request, RestaurantTable.class);
    }

    public CompletableFuture<Void> deleteTable(Long id) {
        return deleteAsync("/api/admin/tables/" + id);
    }

    public CompletableFuture<QRCodeResponse> generateQRCode(Long tableId) {
        return postAsync("/api/admin/tables/" + tableId + "/qr", null, QRCodeResponse.class);
    }

    // Reports
    public CompletableFuture<RevenueReport> getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) {
        String path = String.format("/api/admin/reports/revenue?start=%s&end=%s",
                URLEncoder.encode(startDate.toString(), StandardCharsets.UTF_8),
                URLEncoder.encode(endDate.toString(), StandardCharsets.UTF_8));
        return getAsync(path, RevenueReport.class);
    }

    // Generic HTTP Methods
    private <T> CompletableFuture<T> getAsync(String path, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(baseUrl + path);
                addAuthHeader(request);
                return executeRequest(request, responseType);
            } catch (Exception e) {
                log.error("GET request failed: {}", path, e);
                throw new RuntimeException("Request failed: " + e.getMessage());
            }
        });
    }

    private <T> CompletableFuture<T> getAsync(String path, TypeReference<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(baseUrl + path);
                addAuthHeader(request);
                return executeRequest(request, responseType);
            } catch (Exception e) {
                log.error("GET request failed: {}", path, e);
                throw new RuntimeException("Request failed: " + e.getMessage());
            }
        });
    }

    private <T, R> CompletableFuture<R> postAsync(String path, T body, Class<R> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost request = new HttpPost(baseUrl + path);
                addAuthHeader(request);
                request.setHeader("Content-Type", "application/json");
                
                if (body != null) {
                    String json = objectMapper.writeValueAsString(body);
                    request.setEntity(EntityBuilder.create()
                            .setText(json)
                            .setContentType(ContentType.APPLICATION_JSON)
                            .build());
                }

                return executeRequest(request, responseType);
            } catch (Exception e) {
                log.error("POST request failed: {}", path, e);
                throw new RuntimeException("Request failed: " + e.getMessage());
            }
        });
    }

    private <T, R> CompletableFuture<R> putAsync(String path, T body, Class<R> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPut request = new HttpPut(baseUrl + path);
                addAuthHeader(request);
                request.setHeader("Content-Type", "application/json");
                
                String json = objectMapper.writeValueAsString(body);
                request.setEntity(EntityBuilder.create()
                        .setText(json)
                        .setContentType(ContentType.APPLICATION_JSON)
                        .build());

                return executeRequest(request, responseType);
            } catch (Exception e) {
                log.error("PUT request failed: {}", path, e);
                throw new RuntimeException("Request failed: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<Void> deleteAsync(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpDelete request = new HttpDelete(baseUrl + path);
                addAuthHeader(request);
                executeRequest(request, Void.class);
                return null;
            } catch (Exception e) {
                log.error("DELETE request failed: {}", path, e);
                throw new RuntimeException("Request failed: " + e.getMessage());
            }
        });
    }

    private void addAuthHeader(HttpUriRequestBase request) {
        if (authToken != null) {
            request.setHeader("Authorization", "Bearer " + authToken);
        }
    }

private <T> T executeRequest(HttpUriRequestBase request, Class<T> responseType) throws IOException, ParseException {
        try (var response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            int statusCode = response.getCode();
            
            String responseBody = entity != null ? EntityUtils.toString(entity) : "";
            
            if (statusCode >= 400) {
                String errorMsg = parseErrorMessage(responseBody);
                throw new IOException("HTTP " + statusCode + ": " + errorMsg);
            }
            
            if (responseType == Void.class) {
                return null;
            }
            
            if (responseBody.isEmpty()) {
                return null;
            }
            
            // Use TypeFactory to properly deserialize generic ApiResponse<T>
            JavaType apiResponseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, responseType);
            ApiResponse<T> apiResponse = objectMapper.readValue(responseBody, apiResponseType);
            
            if (!apiResponse.isSuccess()) {
                throw new IOException(apiResponse.getMessage());
            }
            
            return apiResponse.getData();
        }
    }
    
    private <T> T executeRequest(HttpUriRequestBase request, TypeReference<T> responseType) throws IOException, ParseException {
        try (var response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            int statusCode = response.getCode();
            
            String responseBody = entity != null ? EntityUtils.toString(entity) : "";
            
            if (statusCode >= 400) {
                String errorMsg = parseErrorMessage(responseBody);
                throw new IOException("HTTP " + statusCode + ": " + errorMsg);
            }
            
            // Construct ApiResponse<T> type from TypeReference
            JavaType dataType = objectMapper.getTypeFactory().constructType(responseType);
            JavaType apiResponseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, dataType);
            ApiResponse<T> apiResponse = objectMapper.readValue(responseBody, apiResponseType);
            
            if (!apiResponse.isSuccess()) {
                throw new IOException(apiResponse.getMessage());
            }
            
            return apiResponse.getData();
        }
    }

    private String parseErrorMessage(String responseBody) {
        try {
            var apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
            return apiResponse.getMessage();
        } catch (Exception e) {
            return "Unknown error";
        }
    }

    // Request DTOs
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class LoginRequest {
        private String email;
        private String password;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class OrderStatusUpdateRequest {
        private String newStatus;
        private String changedBy;
        private String changeReason;
    }

    // Menu Item Request
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class MenuItemRequest {
        private Long categoryId;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        private String foodType;
        private Boolean isAvailable;
        private Integer preparationTimeMinutes;
        private Boolean isFeatured;
        private Integer displayOrder;
    }

    // Category Request
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CategoryRequest {
        private String name;
        private String description;
        private String imageUrl;
        private Integer displayOrder;
        private Boolean isActive;
    }

    // Table Request
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TableRequest {
        private String tableNumber;
        private String tableName;
        private Integer capacity;
        private String status;
        private Boolean isActive;
    }

    // QR Code Response
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QRCodeResponse {
        private Long id;
        private Long tableId;
        private String tableNumber;
        private String tableName;
        private String token;
        private String qrData;
        private String imageUrl;
        private Boolean isActive;
        private Integer scanCount;
    }

    // Generic API Response
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String errorCode;
        private Object timestamp; // Accept timestamp from backend
    }
}