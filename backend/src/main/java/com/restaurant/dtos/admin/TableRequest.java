package com.restaurant.dtos.admin;

import com.restaurant.entity.RestaurantTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableRequest {

    @NotBlank(message = "Table number is required")
    @Size(max = 20, message = "Table number must not exceed 20 characters")
    private String tableNumber;

    @NotBlank(message = "Table name is required")
    @Size(max = 50, message = "Table name must not exceed 50 characters")
    private String tableName;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    private RestaurantTable.TableStatus status;

    private Boolean isActive;
}
