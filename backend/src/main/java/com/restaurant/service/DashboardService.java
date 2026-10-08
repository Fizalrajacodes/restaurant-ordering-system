package com.restaurant.service;

import com.restaurant.dtos.admin.CategoryRevenueResponse;
import com.restaurant.dtos.admin.DashboardResponse;
import com.restaurant.dtos.admin.DailyRevenueResponse;
import com.restaurant.dtos.admin.RecentOrderResponse;
import com.restaurant.dtos.admin.RevenueReportResponse;
import com.restaurant.dtos.admin.TopSellingItemResponse;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.repository.OrderItemRepository;
import com.restaurant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardResponse getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        long totalOrders = orderRepository.count();
        long todaysOrders = orderRepository.countOrdersSince(startOfDay, Order.OrderStatus.CANCELLED);
        long pendingOrders = orderRepository.countByOrderStatus(Order.OrderStatus.PENDING);
        long preparingOrders = orderRepository.countByOrderStatus(Order.OrderStatus.PREPARING);
        long readyOrders = orderRepository.countByOrderStatus(Order.OrderStatus.READY);
        long completedOrders = orderRepository.countByOrderStatus(Order.OrderStatus.SERVED);
        long cancelledOrders = orderRepository.countByOrderStatus(Order.OrderStatus.CANCELLED);

        BigDecimal todaysRevenue = orderRepository.getRevenueSince(startOfDay, Order.PaymentStatus.PAYMENT_COMPLETED);
        if (todaysRevenue == null) todaysRevenue = BigDecimal.ZERO;

        BigDecimal totalRevenue = orderRepository.getRevenueSince(LocalDateTime.of(2000, 1, 1, 0, 0), Order.PaymentStatus.PAYMENT_COMPLETED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        List<RecentOrderResponse> recentOrders = orderRepository.findByOrderStatusIn(
                List.of(Order.OrderStatus.PENDING, Order.OrderStatus.CONFIRMED, Order.OrderStatus.PREPARING, Order.OrderStatus.READY),
                org.springframework.data.domain.PageRequest.of(0, 10)
        ).stream()
                .map(this::mapToRecentOrder)
                .collect(Collectors.toList());

        List<TopSellingItemResponse> topSellingItems = getTopSellingItems(startOfDay);

        return DashboardResponse.builder()
                .totalOrders(totalOrders)
                .todaysOrders(todaysOrders)
                .pendingOrders(pendingOrders)
                .preparingOrders(preparingOrders)
                .readyOrders(readyOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .todaysRevenue(todaysRevenue)
                .totalRevenue(totalRevenue)
                .recentOrders(recentOrders)
                .topSellingItems(topSellingItems)
                .build();
    }

    public RevenueReportResponse getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalRevenue = orderRepository.getRevenueSince(startDate, Order.PaymentStatus.PAYMENT_COMPLETED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long totalOrders = orderRepository.countOrdersSince(startDate, Order.OrderStatus.CANCELLED);
        long completedOrders = orderRepository.countByOrderStatus(Order.OrderStatus.SERVED);
        long cancelledOrders = orderRepository.countByOrderStatus(Order.OrderStatus.CANCELLED);

        List<DailyRevenueResponse> dailyRevenue = getDailyRevenue(startDate, endDate);
        List<CategoryRevenueResponse> categoryRevenue = getCategoryRevenue(startDate, endDate);

        return RevenueReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .dailyRevenue(dailyRevenue)
                .categoryRevenue(categoryRevenue)
                .build();
    }

    private List<TopSellingItemResponse> getTopSellingItems(LocalDateTime startDate) {
        return orderItemRepository.getMostOrderedItems(startDate, Order.OrderStatus.CANCELLED).stream()
                .limit(10)
                .map(row -> TopSellingItemResponse.builder()
                        .itemName((String) row[0])
                        .totalQuantity(((Number) row[1]).longValue())
                        .totalRevenue((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    private List<DailyRevenueResponse> getDailyRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation - in production use native query
        return java.util.Collections.emptyList();
    }

    private List<CategoryRevenueResponse> getCategoryRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation - in production use native query
        return java.util.Collections.emptyList();
    }

    private RecentOrderResponse mapToRecentOrder(Order order) {
        return RecentOrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTable().getTableNumber())
                .customerName(order.getCustomerName())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}