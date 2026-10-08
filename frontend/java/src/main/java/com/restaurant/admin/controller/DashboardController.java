package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import com.restaurant.admin.model.Dashboard;
import com.restaurant.admin.model.RecentOrder;
import com.restaurant.admin.model.TopSellingItem;
import com.restaurant.admin.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    // Stat cards
    @FXML private VBox totalOrdersCard;
    @FXML private VBox todaysOrdersCard;
    @FXML private VBox pendingOrdersCard;
    @FXML private VBox preparingOrdersCard;
    @FXML private VBox readyOrdersCard;
    @FXML private VBox completedOrdersCard;
    @FXML private VBox cancelledOrdersCard;
    @FXML private VBox todaysRevenueCard;
    @FXML private VBox totalRevenueCard;

    // Stat card value labels (assuming structure: title label + value label)
    @FXML private Label totalOrdersValue;
    @FXML private Label todaysOrdersValue;
    @FXML private Label pendingOrdersValue;
    @FXML private Label preparingOrdersValue;
    @FXML private Label readyOrdersValue;
    @FXML private Label completedOrdersValue;
    @FXML private Label todaysRevenueValue;
    @FXML private Label totalRevenueValue;

    // Recent orders table
    @FXML private TableView<RecentOrder> recentOrdersTable;
    @FXML private TableColumn<RecentOrder, String> colRecentOrderNumber;
    @FXML private TableColumn<RecentOrder, String> colRecentTable;
    @FXML private TableColumn<RecentOrder, String> colRecentCustomer;
    @FXML private TableColumn<RecentOrder, String> colRecentAmount;
    @FXML private TableColumn<RecentOrder, String> colRecentStatus;

    // Top selling items table
    @FXML private TableView<TopSellingItem> topItemsTable;
    @FXML private TableColumn<TopSellingItem, String> colTopItemName;
    @FXML private TableColumn<TopSellingItem, String> colTopItemQuantity;
    @FXML private TableColumn<TopSellingItem, String> colTopItemRevenue;

    @FXML private VBox recentOrdersList;
    @FXML private VBox topItemsList;

    private AdminApplication app;
    private ApiService apiService;
    private ObservableList<RecentOrder> recentOrdersData = FXCollections.observableArrayList();
    private ObservableList<TopSellingItem> topItemsData = FXCollections.observableArrayList();

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();
        initializeTables();
        loadDashboardData();
    }

    private void initializeTables() {
        // Recent Orders Table
        if (recentOrdersTable != null) {
            colRecentOrderNumber.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getOrderNumber()));
            colRecentTable.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getTableNumber()));
            colRecentCustomer.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getCustomerName()));
            colRecentAmount.setCellValueFactory(data -> {
                BigDecimal amount = data.getValue().getTotalAmount();
                return new SimpleStringProperty(amount != null ? formatCurrency(amount) : "₹0.00");
            });
            colRecentStatus.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getOrderStatus()));

            recentOrdersTable.setItems(recentOrdersData);
        }

        // Top Selling Items Table
        if (topItemsTable != null) {
            colTopItemName.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getItemName()));
            colTopItemQuantity.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getTotalQuantity().toString()));
            colTopItemRevenue.setCellValueFactory(data -> {
                BigDecimal revenue = data.getValue().getTotalRevenue();
                return new SimpleStringProperty(revenue != null ? formatCurrency(revenue) : "₹0.00");
            });

            topItemsTable.setItems(topItemsData);
        }
    }

    public void loadDashboardData() {
        CompletableFuture<Dashboard> future = apiService.getDashboard();
        future.thenAccept(this::updateDashboard)
              .exceptionally(throwable -> {
                  Platform.runLater(() -> {
                      log.error("Failed to load dashboard", throwable);
                      if (app != null) {
                          app.showError("Dashboard Error", "Failed to load dashboard: " + throwable.getMessage());
                      }
                  });
                  return null;
              });
    }

    private void updateDashboard(Dashboard dashboard) {
        Platform.runLater(() -> {
            if (dashboard == null) return;

            // Update stat cards
            updateStatValue(totalOrdersValue, String.valueOf(dashboard.getTotalOrders() != null ? dashboard.getTotalOrders() : 0));
            updateStatValue(todaysOrdersValue, String.valueOf(dashboard.getTodaysOrders() != null ? dashboard.getTodaysOrders() : 0));
            updateStatValue(pendingOrdersValue, String.valueOf(dashboard.getPendingOrders() != null ? dashboard.getPendingOrders() : 0));
            updateStatValue(preparingOrdersValue, String.valueOf(dashboard.getPreparingOrders() != null ? dashboard.getPreparingOrders() : 0));
            updateStatValue(readyOrdersValue, String.valueOf(dashboard.getReadyOrders() != null ? dashboard.getReadyOrders() : 0));
            updateStatValue(completedOrdersValue, String.valueOf(dashboard.getCompletedOrders() != null ? dashboard.getCompletedOrders() : 0));
            updateStatValue(todaysRevenueValue, formatCurrency(dashboard.getTodaysRevenue()));
            updateStatValue(totalRevenueValue, formatCurrency(dashboard.getTotalRevenue()));

            // Update recent orders
            if (recentOrdersData != null && dashboard.getRecentOrders() != null) {
                recentOrdersData.setAll(dashboard.getRecentOrders());
                updateRecentOrdersList(dashboard.getRecentOrders());
            }

            // Update top selling items
            if (topItemsData != null && dashboard.getTopSellingItems() != null) {
                topItemsData.setAll(dashboard.getTopSellingItems());
                updateTopItemsList(dashboard.getTopSellingItems());
            }
        });
    }

    private void updateStatValue(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    private void updateRecentOrdersList(List<RecentOrder> orders) {
        if (recentOrdersList == null) return;

        recentOrdersList.getChildren().clear();
        for (RecentOrder order : orders) {
            HBox orderBox = new HBox(15);
            orderBox.setStyle("-fx-padding: 12 20; -fx-border-color: derive(-fx-background, -20%); -fx-border-width: 0 0 1 0;");

            Label orderNumberLabel = new Label(order.getOrderNumber());
            orderNumberLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 150;");

            Label tableLabel = new Label("Table " + order.getTableNumber());
            tableLabel.setStyle("-fx-text-fill: #b8b8b8; -fx-min-width: 80;");

            Label customerLabel = new Label(order.getCustomerName());
            customerLabel.setStyle("-fx-text-fill: #b8b8b8; -fx-min-width: 150;");

            Label amountLabel = new Label(formatCurrency(order.getTotalAmount()));
            amountLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold; -fx-min-width: 100;");

            Label statusLabel = new Label(order.getOrderStatus());
            statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-min-width: 100;");

            Label timeLabel = new Label();
            if (order.getCreatedAt() != null) {
                timeLabel.setText(order.getCreatedAt().format(DateTimeFormatter.ofPattern("hh:mm a")));
            }
            timeLabel.setStyle("-fx-text-fill: #b8b8b8;");

            orderBox.getChildren().addAll(orderNumberLabel, tableLabel, customerLabel, amountLabel, statusLabel, timeLabel);
            recentOrdersList.getChildren().add(orderBox);
        }
    }

    private void updateTopItemsList(List<TopSellingItem> items) {
        if (topItemsList == null) return;

        topItemsList.getChildren().clear();
        int rank = 1;
        for (TopSellingItem item : items) {
            HBox itemBox = new HBox(15);
            itemBox.setStyle("-fx-padding: 12 20; -fx-border-color: derive(-fx-background, -20%); -fx-border-width: 0 0 1 0;");

            Label rankLabel = new Label("#" + rank++);
            rankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e94560; -fx-min-width: 40;");

            Label nameLabel = new Label(item.getItemName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 250;");

            Label quantityLabel = new Label(item.getTotalQuantity() + " sold");
            quantityLabel.setStyle("-fx-text-fill: #b8b8b8; -fx-min-width: 100;");

            Label revenueLabel = new Label(formatCurrency(item.getTotalRevenue()));
            revenueLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");

            itemBox.getChildren().addAll(rankLabel, nameLabel, quantityLabel, revenueLabel);
            topItemsList.getChildren().add(itemBox);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return String.format("₹%,.2f", amount);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDashboardData();
    }

    @FXML
    private void handleViewOrder(ActionEvent event) {
        // Get selected order and show details
        if (recentOrdersTable != null) {
            RecentOrder selectedOrder = recentOrdersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                app.showInfo("Order Details", "Order: " + selectedOrder.getOrderNumber());
            }
        }
    }
}