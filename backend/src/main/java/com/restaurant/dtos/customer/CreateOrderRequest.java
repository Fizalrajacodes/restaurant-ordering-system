package com.restaurant.dtos.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String customerName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number format")
    private String customerMobile;

    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Table number is required")
    private String tableNumber;

    @NotNull(message = "Cart items are required")
    private List<CreateOrderItemRequest> items;

    private String specialInstructions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateOrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private String specialInstructions;
    }
}