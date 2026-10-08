package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import com.restaurant.admin.model.Order;
import com.restaurant.admin.model.PageResponse;
import com.restaurant.admin.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colOrderNumber;
    @FXML private TableColumn<Order, String> colTableNumber;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, String> colCustomerMobile;
    @FXML private TableColumn<Order, Number> colItemsCount;
    @FXML private TableColumn<Order, String> colTotalAmount;
    @FXML private TableColumn<Order, String> colOrderStatus;
    @FXML private TableColumn<Order, String> colPaymentStatus;
    @FXML private TableColumn<Order, String> colOrderTime;
    @FXML private TableColumn<Order, Void> colActions;

    @FXML private ComboBox<String> orderStatusFilter;
    @FXML private TextField orderSearchField;
    @FXML private Button filterBtn;
    @FXML private Button exportBtn;
    @FXML private Label paginationInfo;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;

    private AdminApplication app;
    private ApiService apiService;
    private ObservableList<Order> ordersData = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int pageSize = 50;
    private String currentStatusFilter = null;

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();
        initializeTable();
        initializeFilters();
        loadOrders();
    }

    private void initializeFilters() {
        orderStatusFilter.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING", "CONFIRMED", "PREPARING", "READY", "SERVED", "CANCELLED"
        ));
        orderStatusFilter.setValue("ALL");
    }

    private void initializeTable() {
        // Order Number
        colOrderNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderNumber()));

        // Table Number
        colTableNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTableNumber()));

        // Customer Name
        colCustomerName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));

        // Customer Mobile
        colCustomerMobile.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerMobile()));

        // Items Count
        colItemsCount.setCellValueFactory(data -> {
            int count = data.getValue().getItems() != null ? data.getValue().getItems().size() : 0;
            return new javafx.beans.property.SimpleIntegerProperty(count);
        });

        // Total Amount
        colTotalAmount.setCellValueFactory(data -> {
            String amount = data.getValue().getTotalAmount() != null
                    ? String.format("₹%,.2f", data.getValue().getTotalAmount())
                    : "₹0.00";
            return new SimpleStringProperty(amount);
        });

        // Order Status with badge
        colOrderStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderStatus()));
        colOrderStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(status);
                    getStyleClass().add(getStatusBadgeClass(status));
                }
            }
        });

        // Payment Status
        colPaymentStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentStatus()));
        colPaymentStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(status);
                    getStyleClass().add(getPaymentStatusClass(status));
                }
            }
        });

        // Order Time
        colOrderTime.setCellValueFactory(data -> {
            String time = data.getValue().getCreatedAt() != null
                    ? data.getValue().getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                    : "";
            return new SimpleStringProperty(time);
        });

        // Actions
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button statusBtn = new Button("Update");

            {
                viewBtn.getStyleClass().add("action-button");
                statusBtn.getStyleClass().add("action-button");

                viewBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewOrder(order);
                });

                statusBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5, viewBtn, statusBtn);
                    hbox.setStyle("-fx-alignment: CENTER;");
                    setGraphic(hbox);
                }
            }
        });

        ordersTable.setItems(ordersData);
    }

    @FXML
    private void handleOrderFilter(ActionEvent event) {
        currentStatusFilter = orderStatusFilter.getValue();
        if ("ALL".equals(currentStatusFilter)) {
            currentStatusFilter = null;
        }
        currentPage = 0;
        loadOrders();
    }

    @FXML
    private void handlePrevPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            loadOrders();
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        currentPage++;
        loadOrders();
    }

    private void loadOrders() {
        setLoading(true);

        CompletableFuture<PageResponse<Order>> future;
        if (currentStatusFilter != null) {
            future = apiService.getOrders(currentStatusFilter, currentPage, pageSize);
        } else {
            future = apiService.getOrders(null, currentPage, pageSize);
        }

        future.thenAccept(pageResponse -> {
            Platform.runLater(() -> {
                ordersData.setAll(pageResponse.getContent());
                updatePaginationInfo(pageResponse);
                setLoading(false);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                app.showError("Orders Error", "Failed to load orders: " + throwable.getMessage());
                setLoading(false);
            });
            return null;
        });
    }

    private void updatePaginationInfo(PageResponse<Order> pageResponse) {
        int start = pageResponse.getPageNumber() * pageResponse.getPageSize() + 1;
        int end = Math.min((pageResponse.getPageNumber() + 1) * pageResponse.getPageSize(), (int) pageResponse.getTotalElements());
        paginationInfo.setText(String.format("Showing %d-%d of %d orders", start, end, pageResponse.getTotalElements()));
        prevPageBtn.setDisable(pageResponse.isFirst());
        nextPageBtn.setDisable(pageResponse.isLast());
    }

    private void handleViewOrder(Order order) {
        // Show order detail dialog
        app.showInfo("Order Details", "Order: " + order.getOrderNumber() + "\nCustomer: " + order.getCustomerName());
    }

    private void handleUpdateStatus(Order order) {
        // Show status update dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>("CONFIRMED", "CONFIRMED", "PREPARING", "READY", "SERVED", "CANCELLED");
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update status for order: " + order.getOrderNumber());
        dialog.setContentText("Select new status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            setLoading(true);
            apiService.updateOrderStatus(order.getId(), newStatus, "Admin", "Status updated from dashboard")
                    .thenAccept(updatedOrder -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            loadOrders();
                            app.showInfo("Status Updated", "Order " + order.getOrderNumber() + " updated to " + newStatus);
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Update Failed", "Failed to update order status: " + throwable.getMessage());
                        });
                        return null;
                    });
        });
    }

    private void setLoading(boolean loading) {
        ordersTable.setDisable(loading);
        filterBtn.setDisable(loading);
    }

    private String getStatusBadgeClass(String status) {
        return switch (status) {
            case "PENDING" -> "badge-pending";
            case "CONFIRMED" -> "badge-confirmed";
            case "PREPARING" -> "badge-preparing";
            case "READY" -> "badge-ready";
            case "SERVED" -> "badge-served";
            case "CANCELLED" -> "badge-cancelled";
            default -> "";
        };
    }

    private String getPaymentStatusClass(String status) {
        return switch (status) {
            case "PAYMENT_PENDING" -> "badge-payment-pending";
            case "PAYMENT_COMPLETED" -> "badge-payment-completed";
            case "PAYMENT_FAILED" -> "badge-payment-failed";
            default -> "";
        };
    }
}