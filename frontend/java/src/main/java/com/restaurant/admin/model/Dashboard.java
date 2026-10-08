package com.restaurant.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dashboard {
    private Long totalOrders;
    private Long todaysOrders;
    private Long pendingOrders;
    private Long preparingOrders;
    private Long readyOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private BigDecimal todaysRevenue;
    private BigDecimal totalRevenue;
    private List<RecentOrder> recentOrders;
    private List<TopSellingItem> topSellingItems;
}