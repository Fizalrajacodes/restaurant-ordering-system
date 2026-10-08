package com.restaurant.dtos.customer;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {

    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();

    private String tableNumber;
    private String tableName;

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTax(BigDecimal taxRate) {
        return getSubtotal().multiply(taxRate).divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getTotal(BigDecimal taxRate) {
        return getSubtotal().add(getTax(taxRate)).subtract(getDiscount());
    }

    public BigDecimal getDiscount() {
        return BigDecimal.ZERO;
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItemDTO::getQuantity).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}