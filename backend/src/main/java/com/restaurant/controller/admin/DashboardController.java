package com.restaurant.controller.admin;

import com.restaurant.dtos.ApiResponse;
import com.restaurant.dtos.admin.DashboardResponse;
import com.restaurant.dtos.admin.RevenueReportResponse;
import com.restaurant.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.success(dashboardService.getDashboardStats());
    }

    @GetMapping("/revenue")
    public ApiResponse<RevenueReportResponse> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.success(dashboardService.getRevenueReport(startDate, endDate));
    }
}