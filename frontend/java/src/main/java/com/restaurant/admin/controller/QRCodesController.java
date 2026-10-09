package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QRCodesController {

    private static final Logger log = LoggerFactory.getLogger(QRCodesController.class);

    @FXML private TableView<RestaurantTable> qrCodesTable;
    @FXML private TableColumn<RestaurantTable, String> colQrTableNumber;
    @FXML private TableColumn<RestaurantTable, String> colQrTableName;
    @FXML private TableColumn<RestaurantTable, String> colQrToken;
    @FXML private TableColumn<RestaurantTable, String> colQrUrl;
    @FXML private TableColumn<RestaurantTable, Number> colQrScans;
    @FXML private TableColumn<RestaurantTable, String> colQrLastScan;
    @FXML private TableColumn<RestaurantTable, Void> colQrActions;

    @FXML private Button generateAllQrBtn;
    @FXML private Button printAllQrBtn;
    @FXML private VBox qrPreviewPane;
    @FXML private Label previewTableLabel;
    @FXML private ImageView previewQrImage;
    @FXML private Label previewUrlLabel;
    @FXML private Button closePreviewBtn;
    @FXML private Button downloadQrBtn;
    @FXML private Button printQrBtn;

    private AdminApplication app;
    private ApiService apiService;
    private ObservableList<RestaurantTable> tablesData = FXCollections.observableArrayList();
    private List<Category> categories;
    private List<MenuItem> menuItems;

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();
        initializeTable();
        loadData();
    }

    private void initializeTable() {
        colQrTableNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTableNumber()));
        colQrTableName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTableName()));
        colQrToken.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getQrToken() != null ? data.getValue().getQrToken() : "Not Generated"));
        colQrUrl.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getQrCodeUrl() != null ? data.getValue().getQrCodeUrl() : "Not Generated"));
        colQrScans.setCellValueFactory(data -> {
            // This would come from QR code data
            return new javafx.beans.property.SimpleIntegerProperty(0);
        });
        colQrLastScan.setCellValueFactory(data -> new SimpleStringProperty("N/A"));

        colQrActions.setCellFactory(column -> new TableCell<>() {
            private final Button generateBtn = new Button("Generate");
            private final Button previewBtn = new Button("Preview");
            private final Button printBtn = new Button("Print");

            {
                generateBtn.getStyleClass().add("action-button");
                previewBtn.getStyleClass().add("action-button");
                printBtn.getStyleClass().add("action-button");

                generateBtn.setOnAction(event -> {
                    RestaurantTable table = getTableView().getItems().get(getIndex());
                    handleGenerateQR(table);
                });

                previewBtn.setOnAction(event -> {
                    RestaurantTable table = getTableView().getItems().get(getIndex());
                    handlePreviewQR(table);
                });

                printBtn.setOnAction(event -> {
                    RestaurantTable table = getTableView().getItems().get(getIndex());
                    handlePrintQR(table);
                });
            }

        private void handlePrintQR(RestaurantTable table) {
        if (table.getQrCodeUrl() == null || table.getQrCodeUrl().isEmpty()) {
            app.showInfo("No QR Code", "Please generate QR code first for " + table.getTableNumber());
            return;
        }
        app.showInfo("Print QR Code", "Printing QR code for " + table.getTableNumber() + "\nURL: " + table.getQrCodeUrl());
        // TODO: Implement actual printing
    }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(3, generateBtn, previewBtn, printBtn);
                    hbox.setStyle("-fx-alignment: CENTER;");
                    setGraphic(hbox);
                }
            }
        });

        qrCodesTable.setItems(tablesData);
    }

    @FXML
    private void handleGenerateAllQR(ActionEvent event) {
        if (app.showConfirmation("Generate All QR Codes", "Generate QR codes for all tables?")) {
            setLoading(true);
            int totalTables = tablesData.size();
            int[] completed = {0};

            for (RestaurantTable table : tablesData) {
                apiService.generateQRCode(table.getId())
                        .thenAccept(qrCode -> {
                            Platform.runLater(() -> {
                                completed[0]++;
                                table.setQrToken(qrCode.getToken());
                                table.setQrCodeUrl(qrCode.getImageUrl());
                                if (completed[0] == totalTables) {
                                    setLoading(false);
                                    app.showInfo("Success", "QR codes generated for all tables");
                                    loadData();
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

    @FXML
    private void handlePrintAllQR(ActionEvent event) {
        app.showInfo("Print All QR", "Print all QR codes feature - coming soon!");
    }

    @FXML
    private void handleClosePreview(ActionEvent event) {
        qrPreviewPane.setVisible(false);
    }

    @FXML
    private void handleDownloadQR(ActionEvent event) {
        app.showInfo("Download QR", "Download QR code feature - coming soon!");
    }

    @FXML
    private void handlePrintQR(ActionEvent event) {
        app.showInfo("Print QR", "Print QR code feature - coming soon!");
    }

        private void handleGenerateQR(RestaurantTable table) {
        setLoading(true);
        apiService.generateQRCode(table.getId())
                .thenAccept(qrCode -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        table.setQrToken(qrCode.getToken());
                        table.setQrCodeUrl(qrCode.getImageUrl());
                        qrCodesTable.refresh();
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

    private void handlePreviewQR(RestaurantTable table) {
        if (table.getQrCodeUrl() == null || table.getQrCodeUrl().isEmpty()) {
            app.showInfo("No QR Code", "Please generate QR code first for " + table.getTableNumber());
            return;
        }

        previewTableLabel.setText(table.getTableNumber() + " - " + table.getTableName());
        previewUrlLabel.setText(table.getQrCodeUrl());

        // Load QR image if available
        if (table.getQrCodeUrl() != null && table.getQrCodeUrl().startsWith("data:image")) {
            try {
                String base64 = table.getQrCodeUrl().split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                previewQrImage.setImage(image);
            } catch (Exception e) {
                log.warn("Failed to load QR image", e);
            }
        }

        qrPreviewPane.setVisible(true);
    }

    private void loadData() {
        setLoading(true);

        // Load tables and categories
        CompletableFuture<Void> tablesFuture = apiService.getTables()
                .thenAccept(tables -> {
                    Platform.runLater(() -> {
                        tablesData.setAll(tables);
                        qrCodesTable.refresh();
                    });
                });

        setLoading(false);
    }

    private void setLoading(boolean loading) {
        generateAllQrBtn.setDisable(loading);
        printAllQrBtn.setDisable(loading);
    }
}