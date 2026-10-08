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
public class DashboardResponse {

    private Long totalOrders;
    private Long todaysOrders;
    private Long pendingOrders;
    private Long preparingOrders;
    private Long readyOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private BigDecimal todaysRevenue;
    private BigDecimal totalRevenue;
    private List<RecentOrderResponse> recentOrders;
    private List<TopSellingItemResponse> topSellingItems;
}
