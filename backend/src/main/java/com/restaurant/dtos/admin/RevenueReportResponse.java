package com.restaurant.dtos.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportResponse {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private List<DailyRevenueResponse> dailyRevenue;
    private List<CategoryRevenueResponse> categoryRevenue;
}
