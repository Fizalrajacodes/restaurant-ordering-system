package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TablesController {

    private static final Logger log = LoggerFactory.getLogger(TablesController.class);

    @FXML private javafx.scene.layout.FlowPane tablesFlowPane;
    @FXML private Button addTableBtn;
    @FXML private Button generateAllQrBtn;

    private AdminApplication app;
    private ApiService apiService;
    private ObservableList<RestaurantTable> tablesData = FXCollections.observableArrayList();

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();
        loadTables();
    }

    @FXML
    private void handleAddTable(ActionEvent event) {
        Dialog<ApiService.TableRequest> dialog = new Dialog<>();
        dialog.setTitle("Add Table");
        dialog.setHeaderText("Enter table details:");

        TextField tableNumberField = new TextField();
        tableNumberField.setPromptText("TABLE-01");
        TextField tableNameField = new TextField();
        tableNameField.setPromptText("Table 01");
        TextField capacityField = new TextField();
        capacityField.setPromptText("4");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "INACTIVE");
        statusCombo.setValue("AVAILABLE");

        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Table Number:"), tableNumberField,
                new Label("Table Name:"), tableNameField,
                new Label("Capacity:"), capacityField,
                new Label("Status:"), statusCombo));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    ApiService.TableRequest request = new ApiService.TableRequest();
                    request.setTableNumber(tableNumberField.getText().trim());
                    request.setTableName(tableNameField.getText().trim());
                    request.setCapacity(Integer.parseInt(capacityField.getText()));
                    request.setStatus(statusCombo.getValue());
                    request.setIsActive(true);
                    return request;
                } catch (NumberFormatException e) {
                    app.showError("Validation Error", "Capacity must be a number");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(request -> {
            setLoading(true);
            apiService.createTable(request)
                    .thenAccept(table -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            tablesData.add(table);
                            createTableCard(table);
                            app.showInfo("Success", "Table created successfully");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Error", "Failed to create table: " + throwable.getMessage());
                        });
                        return null;
                    });
        });
    }

    @FXML
    private void handleGenerateAllQR(ActionEvent event) {
        if (app.showConfirmation("Generate QR Codes", "Generate QR codes for all tables?")) {
            setLoading(true);
            int totalTables = tablesData.size();
            int[] completed = {0};

            for (RestaurantTable table : tablesData) {
                apiService.generateQRCode(table.getId())
                        .thenAccept(qrCode -> {
                            Platform.runLater(() -> {
                                completed[0]++;
                                if (completed[0] == totalTables) {
                                    setLoading(false);
                                    app.showInfo("Success", "QR codes generated for all tables");
                                    loadTables();
                                }
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                completed[0]++;
                                if (completed[0] == totalTables) {
                                    setLoading(false);
                                }
                            });
                            return null;
                        });
            }
        }
    }

    private void loadTables() {
        setLoading(true);
        apiService.getTables()
                .thenAccept(tables -> {
                    Platform.runLater(() -> {
                        tablesData.setAll(tables);
                        tablesFlowPane.getChildren().clear();
                        tables.forEach(this::createTableCard);
                        setLoading(false);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        app.showError("Error", "Failed to load tables: " + throwable.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
    }

    private void createTableCard(RestaurantTable table) {
        VBox card = new VBox(10);
        card.getStyleClass().add("table-card");
        card.setStyle(card.getStyle() + String.format(
                "-fx-min-width: 180; -fx-min-height: 120; -fx-padding: 20;"));

        String statusClass = table.getStatus() != null ? table.getStatus().toLowerCase() : "available";

        Label tableNumberLabel = new Label(table.getTableNumber());
        tableNumberLabel.getStyleClass().add("table-number");

        Label statusLabel = new Label(table.getStatus() != null ? table.getStatus() : "UNKNOWN");
        statusLabel.getStyleClass().addAll("table-status", statusClass);

        Label capacityLabel = new Label("Capacity: " + table.getCapacity());
        capacityLabel.getStyleClass().add("text-muted");

        Label ordersLabel = new Label("Active Orders: " + (table.getActiveOrdersCount() != null ? table.getActiveOrdersCount() : 0));
        ordersLabel.getStyleClass().add("text-muted");

        HBox buttons = new HBox(5);
        Button qrBtn = new Button("QR");
        qrBtn.getStyleClass().add("action-button");
        qrBtn.setOnAction(e -> handleGenerateQR(table));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> handleEditTable(table));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-danger");
        deleteBtn.setOnAction(e -> handleDeleteTable(table));

        buttons.getChildren().addAll(qrBtn, editBtn, deleteBtn);
        buttons.setStyle("-fx-alignment: CENTER;");

        card.getChildren().addAll(tableNumberLabel, statusLabel, capacityLabel, ordersLabel, buttons);
        tablesFlowPane.getChildren().add(card);
    }

    private void handleGenerateQR(RestaurantTable table) {
        setLoading(true);
        apiService.generateQRCode(table.getId())
                .thenAccept(qrCode -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        app.showInfo("QR Code Generated",
                                "QR Code generated for " + table.getTableNumber() + "\nURL: " + qrCode.getQrData());
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        app.showError("Error", "Failed to generate QR: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void handleEditTable(RestaurantTable table) {
        Dialog<ApiService.TableRequest> dialog = new Dialog<>();
        dialog.setTitle("Edit Table");
        dialog.setHeaderText("Edit table details:");

        TextField tableNameField = new TextField(table.getTableName());
        TextField capacityField = new TextField(table.getCapacity().toString());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "INACTIVE");
        statusCombo.setValue(table.getStatus());
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(table.getIsActive());

        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Table Name:"), tableNameField,
                new Label("Capacity:"), capacityField,
                new Label("Status:"), statusCombo,
                activeCheck));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    ApiService.TableRequest request = new ApiService.TableRequest();
                    request.setTableNumber(table.getTableNumber());
                    request.setTableName(tableNameField.getText().trim());
                    request.setCapacity(Integer.parseInt(capacityField.getText()));
                    request.setStatus(statusCombo.getValue());
                    request.setIsActive(activeCheck.isSelected());
                    return request;
                } catch (NumberFormatException e) {
                    app.showError("Validation Error", "Capacity must be a number");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(request -> {
            setLoading(true);
            apiService.updateTable(table.getId(), request)
                    .thenAccept(updatedTable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            int index = tablesData.indexOf(table);
                            if (index >= 0) {
                                tablesData.set(index, updatedTable);
                            }
                            loadTables();
                            app.showInfo("Success", "Table updated successfully");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Error", "Failed to update table: " + throwable.getMessage());
                        });
                        return null;
                    });
        });
    }

    private void handleDeleteTable(RestaurantTable table) {
        if (app.showConfirmation("Delete Table", "Are you sure you want to delete " + table.getTableName() + "?")) {
            setLoading(true);
            apiService.deleteTable(table.getId())
                    .thenAccept(v -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            tablesData.remove(table);
                            loadTables();
                            app.showInfo("Success", "Table deleted successfully");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Error", "Failed to delete table: " + throwable.getMessage());
                        });
                        return null;
                    });
        }
    }

    private void setLoading(boolean loading) {
        addTableBtn.setDisable(loading);
        generateAllQrBtn.setDisable(loading);
    }
}