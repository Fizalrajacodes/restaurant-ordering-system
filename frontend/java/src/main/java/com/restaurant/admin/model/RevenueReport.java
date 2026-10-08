package com.restaurant.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private BigDecimal onlineRevenue;
    private BigDecimal cashRevenue;
    private Long totalOrders;
    private Long completedOrders;
    private List<DailyRevenue> dailyRevenue;
    private List<CategoryRevenue> categoryRevenue;
    private List<PaymentMethodRevenue> paymentMethodRevenue;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Long orders;
        
        public Long getOrdersCount() {
            return orders;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryRevenue {
        private String categoryName;
        private BigDecimal revenue;
        private Long orders;
        
        public Long getOrdersCount() {
            return orders;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentMethodRevenue {
        private String paymentMethod;
        private BigDecimal revenue;
        private Long orders;
    }
}