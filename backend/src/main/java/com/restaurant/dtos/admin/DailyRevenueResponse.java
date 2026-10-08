package com.restaurant.dtos.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueResponse {

    private LocalDateTime date;
    private BigDecimal revenue;
    private Long ordersCount;
}
