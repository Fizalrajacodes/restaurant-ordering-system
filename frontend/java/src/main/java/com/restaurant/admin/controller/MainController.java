package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import com.restaurant.admin.model.Dashboard;
import com.restaurant.admin.model.RecentOrder;
import com.restaurant.admin.model.TopSellingItem;
import com.restaurant.admin.model.Order;
import com.restaurant.admin.model.Category;
import com.restaurant.admin.model.MenuItem;
import com.restaurant.admin.model.RestaurantTable;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private VBox navMenu;
    @FXML private BorderPane contentPane;
    @FXML private HBox topBar;
    @FXML private Label pageTitle;
    @FXML private Button refreshBtn;
    @FXML private Button notificationBtn;
    @FXML private TabPane contentTabPane;
    @FXML private Tab dashboardTab, ordersTab, menuTab, categoriesTab, tablesTab, qrCodesTab, reportsTab, settingsTab;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button ordersBtn;
    @FXML private Button menuBtn;
    @FXML private Button categoriesBtn;
    @FXML private Button tablesBtn;
    @FXML private Button qrCodesBtn;
    @FXML private Button reportsBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;

    // Included view controllers (JavaFX convention: fx:id + "Controller")
    @FXML private DashboardController dashboardViewController;
    @FXML private OrdersController ordersViewController;
    @FXML private MenuController menuViewController;
    @FXML private CategoriesController categoriesViewController;
    @FXML private TablesController tablesViewController;
    @FXML private QRCodesController qrCodesViewController;
    @FXML private ReportsController reportsViewController;
    @FXML private SettingsController settingsViewController;

    private AdminApplication app;
    private ApiService apiService;

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();

        // Load user info
        loadUserInfo();

        // Set app reference for all view controllers
        setAppForControllers();

        // Load initial dashboard
        loadDashboard();

        // Set up tab change listener
        contentTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                handleTabSelection(newTab);
            }
        });

        // Set initial active button
        setActiveNavButton(dashboardBtn);
    }

    private void setAppForControllers() {
        if (dashboardViewController != null) dashboardViewController.setApp(app);
        if (ordersViewController != null) ordersViewController.setApp(app);
        if (menuViewController != null) menuViewController.setApp(app);
        if (categoriesViewController != null) categoriesViewController.setApp(app);
        if (tablesViewController != null) tablesViewController.setApp(app);
        if (qrCodesViewController != null) qrCodesViewController.setApp(app);
        if (reportsViewController != null) reportsViewController.setApp(app);
        if (settingsViewController != null) settingsViewController.setApp(app);
    }

    private void loadUserInfo() {
        // TODO: Get user info from API or token
        userNameLabel.setText("Admin User");
        userRoleLabel.setText("Administrator");
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button source = (Button) event.getSource();
        String buttonId = source.getId();

        Tab targetTab = switch (buttonId) {
            case "dashboardBtn" -> dashboardTab;
            case "ordersBtn" -> ordersTab;
            case "menuBtn" -> menuTab;
            case "categoriesBtn" -> categoriesTab;
            case "tablesBtn" -> tablesTab;
            case "qrCodesBtn" -> qrCodesTab;
            case "reportsBtn" -> reportsTab;
            case "settingsBtn" -> settingsTab;
            default -> dashboardTab;
        };

        contentTabPane.getSelectionModel().select(targetTab);
        setActiveNavButton(source);
    }

    @FXML
    private void handleTabSelection(Tab tab) {
        String tabId = tab.getId();

        Button correspondingButton = switch (tabId) {
            case "dashboardTab" -> dashboardBtn;
            case "ordersTab" -> ordersBtn;
            case "menuTab" -> menuBtn;
            case "categoriesTab" -> categoriesBtn;
            case "tablesTab" -> tablesBtn;
            case "qrCodesTab" -> qrCodesBtn;
            case "reportsTab" -> reportsBtn;
            case "settingsTab" -> settingsBtn;
            default -> dashboardBtn;
        };

        setActiveNavButton(correspondingButton);

        // Update page title
        pageTitle.setText(tab.getText());

        // Load data for the selected tab
        loadTabData(tabId);
    }

    private void setActiveNavButton(Button activeButton) {
        // Reset all nav buttons
        for (var child : navMenu.getChildren()) {
            if (child instanceof Button btn) {
                btn.getStyleClass().remove("nav-button-active");
            }
        }

        // Set active button
        if (activeButton != null) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    private void loadTabData(String tabId) {
        switch (tabId) {
            case "dashboardTab" -> loadDashboard();
            case "ordersTab" -> loadOrders();
            case "menuTab" -> loadMenuItems();
            case "categoriesTab" -> loadCategories();
            case "tablesTab" -> loadTables();
            case "qrCodesTab" -> loadQRCodes();
            case "reportsTab" -> loadReports();
            case "settingsTab" -> loadSettings();
        }
    }

    @FXML
    private void handleRefresh() {
        String selectedTab = contentTabPane.getSelectionModel().getSelectedItem().getId();
        loadTabData(selectedTab);
    }

    @FXML
    private void handleNotifications() {
        // TODO: Show notifications panel
        app.showInfo("Notifications", "Notifications feature coming soon!");
    }

    @FXML
    private void handleLogout() {
        if (app.showConfirmation("Logout", "Are you sure you want to logout?")) {
            app.showLoginScreen();
        }
    }

    // Dashboard
    private void loadDashboard() {
        pageTitle.setText("Dashboard");

        CompletableFuture<Dashboard> future = apiService.getDashboard();
        future.thenAccept(this::updateDashboardUI)
              .exceptionally(throwable -> {
                  Platform.runLater(() -> app.showError("Dashboard Error", "Failed to load dashboard: " + throwable.getMessage()));
                  return null;
              });
    }

    private void updateDashboardUI(Dashboard dashboard) {
        Platform.runLater(() -> {
            if (dashboard == null) return;

            // Update stat cards
            updateStatCard("totalOrdersCard", dashboard.getTotalOrders());
            updateStatCard("todaysOrdersCard", dashboard.getTodaysOrders());
            updateStatCard("pendingOrdersCard", dashboard.getPendingOrders());
            updateStatCard("preparingOrdersCard", dashboard.getPreparingOrders());
            updateStatCard("readyOrdersCard", dashboard.getReadyOrders());
            updateStatCard("completedOrdersCard", dashboard.getCompletedOrders());
            updateStatCard("todaysRevenueCard", formatCurrency(dashboard.getTodaysRevenue()));
            updateStatCard("totalRevenueCard", formatCurrency(dashboard.getTotalRevenue()));

            // Update recent orders table
            if (dashboard.getRecentOrders() != null) {
                updateRecentOrdersTable(dashboard.getRecentOrders());
            }

            // Update top selling items
            if (dashboard.getTopSellingItems() != null) {
                updateTopSellingItems(dashboard.getTopSellingItems());
            }
        });
    }

    private void updateStatCard(String cardId, Object value) {
        // This would update the stat cards in the dashboard view
        // Implementation depends on the dashboard.fxml structure
    }

    private void updateRecentOrdersTable(List<RecentOrder> orders) {
        // Update recent orders table
    }

    private void updateTopSellingItems(List<TopSellingItem> items) {
        // Update top selling items
    }

    // Orders
    private void loadOrders() {
        pageTitle.setText("Orders");

        // Load orders with default filters
        apiService.getOrders(null, 0, 50)
            .thenAccept(pageResponse -> updateOrdersTable(pageResponse.getContent()))
            .exceptionally(throwable -> {
                Platform.runLater(() -> app.showError("Orders Error", "Failed to load orders: " + throwable.getMessage()));
                return null;
            });
    }

    private void updateOrdersTable(List<Order> orders) {
        Platform.runLater(() -> {
            // This would be handled by the OrdersController
        });
    }

    // Menu Items
    private void loadMenuItems() {
        pageTitle.setText("Menu Management");

        apiService.getMenuItems()
            .thenAccept(this::updateMenuItemsTable)
            .exceptionally(throwable -> {
                Platform.runLater(() -> app.showError("Menu Error", "Failed to load menu items: " + throwable.getMessage()));
                return null;
            });
    }

    private void updateMenuItemsTable(List<MenuItem> items) {
        Platform.runLater(() -> {
            // This would be handled by the MenuController
        });
    }

    // Categories
    private void loadCategories() {
        pageTitle.setText("Categories");

        apiService.getCategories()
            .thenAccept(this::updateCategoriesTable)
            .exceptionally(throwable -> {
                Platform.runLater(() -> app.showError("Categories Error", "Failed to load categories: " + throwable.getMessage()));
                return null;
            });
    }

    private void updateCategoriesTable(List<Category> categories) {
        Platform.runLater(() -> {
            // This would be handled by the CategoriesController
        });
    }

    // Tables
    private void loadTables() {
        pageTitle.setText("Table Management");

        apiService.getTables()
            .thenAccept(this::updateTablesTable)
            .exceptionally(throwable -> {
                Platform.runLater(() -> app.showError("Tables Error", "Failed to load tables: " + throwable.getMessage()));
                return null;
            });
    }

    private void updateTablesTable(List<RestaurantTable> tables) {
        Platform.runLater(() -> {
            // This would be handled by the TablesController
        });
    }

    // QR Codes
    private void loadQRCodes() {
        pageTitle.setText("QR Codes");
        // Load QR codes for all tables
    }

    // Reports
    private void loadReports() {
        pageTitle.setText("Reports");
        // Load reports
    }

    // Settings
    private void loadSettings() {
        pageTitle.setText("Settings");
        // Load settings
    }

    // Utility methods
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return String.format("₹%,.2f", amount);
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    private String getStatusBadgeClass(String status) {
        return switch (status) {
            case "PENDING" -> "badge-pending";
            case "CONFIRMED" -> "badge-confirmed";
            case "PREPARING" -> "badge-preparing";
            case "READY" -> "badge-ready";
            case "SERVED" -> "badge-served";
            case "CANCELLED" -> "badge-cancelled";
            case "PAYMENT_PENDING" -> "badge-payment-pending";
            case "PAYMENT_COMPLETED" -> "badge-payment-completed";
            case "PAYMENT_FAILED" -> "badge-payment-failed";
            default -> "badge-default";
        };
    }
}