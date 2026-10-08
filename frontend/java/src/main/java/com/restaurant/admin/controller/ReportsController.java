package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import com.restaurant.admin.model.Dashboard;
import com.restaurant.admin.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReportsController {

    private static final Logger log = LoggerFactory.getLogger(ReportsController.class);

    @FXML private DatePicker reportStartDate;
    @FXML private DatePicker reportEndDate;
    @FXML private Button generateReportBtn;
    @FXML private Button exportReportBtn;

    @FXML private VBox totalRevenueCard;
    @FXML private VBox totalOrdersCard;
    @FXML private VBox completedOrdersCard;
    @FXML private VBox avgOrderValueCard;

    @FXML private TableView<DailyRevenueRow> dailyRevenueTable;
    @FXML private TableColumn<DailyRevenueRow, String> colReportDate;
    @FXML private TableColumn<DailyRevenueRow, String> colReportRevenue;
    @FXML private TableColumn<DailyRevenueRow, String> colReportOrders;
    @FXML private TableColumn<DailyRevenueRow, String> colReportAvgOrder;

    @FXML private TableView<CategoryRevenueRow> categoryRevenueTable;
    @FXML private TableColumn<CategoryRevenueRow, String> colCatRevenueName;
    @FXML private TableColumn<CategoryRevenueRow, String> colCatRevenueAmount;
    @FXML private TableColumn<CategoryRevenueRow, String> colCatRevenueOrders;
    @FXML private TableColumn<CategoryRevenueRow, String> colCatRevenuePercent;

    private AdminApplication app;
    private ApiService apiService;
    private ObservableList<DailyRevenueRow> dailyRevenueData = FXCollections.observableArrayList();
    private ObservableList<CategoryRevenueRow> categoryRevenueData = FXCollections.observableArrayList();

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();
        initializeTables();

        // Set default date range (last 30 days)
        reportEndDate.setValue(LocalDate.now());
        reportStartDate.setValue(LocalDate.now().minusDays(30));
    }

    private void initializeTables() {
        // Daily Revenue Table
        colReportDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        colReportRevenue.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRevenue()));
        colReportOrders.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrders()));
        colReportAvgOrder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAvgOrder()));

        dailyRevenueTable.setItems(dailyRevenueData);

        // Category Revenue Table
        colCatRevenueName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        colCatRevenueAmount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRevenue()));
        colCatRevenueOrders.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrders()));
        colCatRevenuePercent.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPercent()));

        categoryRevenueTable.setItems(categoryRevenueData);
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        if (reportStartDate.getValue() == null || reportEndDate.getValue() == null) {
            app.showError("Validation Error", "Please select both start and end dates");
            return;
        }

        LocalDateTime startDate = reportStartDate.getValue().atStartOfDay();
        LocalDateTime endDate = reportEndDate.getValue().atTime(23, 59, 59);

        setLoading(true);

        apiService.getRevenueReport(startDate, endDate)
                .thenAccept(report -> {
                    Platform.runLater(() -> {
                        updateReportUI(report);
                        setLoading(false);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        app.showError("Error", "Failed to generate report: " + throwable.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
    }

    @FXML
    private void handleExportReport(ActionEvent event) {
        app.showInfo("Export", "CSV export feature - coming soon!");
    }

    private void updateReportUI(com.restaurant.admin.model.RevenueReport report) {
        if (report == null) return;

        // Update summary cards
        updateReportCard(totalRevenueCard, "Total Revenue", formatCurrency(report.getTotalRevenue()));
        updateReportCard(totalOrdersCard, "Total Orders", String.valueOf(report.getTotalOrders()));
        updateReportCard(completedOrdersCard, "Completed Orders", String.valueOf(report.getCompletedOrders()));

        BigDecimal avgOrder = report.getTotalOrders() > 0
                ? report.getTotalRevenue().divide(BigDecimal.valueOf(report.getTotalOrders()), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        updateReportCard(avgOrderValueCard, "Avg Order Value", formatCurrency(avgOrder));

        // Update daily revenue table
        dailyRevenueData.clear();
        if (report.getDailyRevenue() != null) {
            report.getDailyRevenue().forEach(day -> {
                DailyRevenueRow row = new DailyRevenueRow();
                row.setDate(day.getDate() != null ? day.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "");
                row.setRevenue(formatCurrency(day.getRevenue()));
                row.setOrders(String.valueOf(day.getOrdersCount()));
                row.setAvgOrder(formatCurrency(
                        day.getOrdersCount() > 0 ? day.getRevenue().divide(BigDecimal.valueOf(day.getOrdersCount()), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO));
                dailyRevenueData.add(row);
            });
        }

        // Update category revenue table
        categoryRevenueData.clear();
        if (report.getCategoryRevenue() != null) {
            report.getCategoryRevenue().forEach(cat -> {
                CategoryRevenueRow row = new CategoryRevenueRow();
                row.setCategoryName(cat.getCategoryName());
                row.setRevenue(formatCurrency(cat.getRevenue()));
                row.setOrders(String.valueOf(cat.getOrdersCount()));
                row.setPercent(report.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0
                        ? String.format("%.1f%%", cat.getRevenue().multiply(BigDecimal.valueOf(100)).divide(report.getTotalRevenue(), 1, BigDecimal.ROUND_HALF_UP))
                        : "0.0%");
                categoryRevenueData.add(row);
            });
        }
    }

    private void updateReportCard(VBox card, String title, String value) {
        if (card.getChildren().size() >= 2) {
            Label titleLabel = (Label) card.getChildren().get(0);
            Label valueLabel = (Label) card.getChildren().get(1);
            titleLabel.setText(title);
            valueLabel.setText(value);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return String.format("₹%,.2f", amount);
    }

    private void setLoading(boolean loading) {
        generateReportBtn.setDisable(loading);
        exportReportBtn.setDisable(loading);
    }

    // Row classes for tables
    public static class DailyRevenueRow {
        private String date;
        private String revenue;
        private String orders;
        private String avgOrder;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getRevenue() { return revenue; }
        public void setRevenue(String revenue) { this.revenue = revenue; }
        public String getOrders() { return orders; }
        public void setOrders(String orders) { this.orders = orders; }
        public String getAvgOrder() { return avgOrder; }
        public void setAvgOrder(String avgOrder) { this.avgOrder = avgOrder; }
    }

    public static class CategoryRevenueRow {
        private String categoryName;
        private String revenue;
        private String orders;
        private String percent;

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getRevenue() { return revenue; }
        public void setRevenue(String revenue) { this.revenue = revenue; }
        public String getOrders() { return orders; }
        public void setOrders(String orders) { this.orders = orders; }
        public String getPercent() { return percent; }
        public void setPercent(String percent) { this.percent = percent; }
    }
}