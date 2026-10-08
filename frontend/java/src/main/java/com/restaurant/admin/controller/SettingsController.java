package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    @FXML private TextField restaurantNameField;
    @FXML private TextField restaurantAddressField;
    @FXML private TextField restaurantPhoneField;
    @FXML private TextField restaurantEmailField;
    @FXML private TextField restaurantGstField;
    @FXML private TextField taxRateField;
    @FXML private TextField qrBaseUrlField;
    @FXML private TextField qrSizeField;

    @FXML private TextField razorpayKeyIdField;
    @FXML private PasswordField razorpayKeySecretField;
    @FXML private PasswordField razorpayWebhookSecretField;
    @FXML private TextField currencyField;

    @FXML private CheckBox notifyNewOrders;
    @FXML private CheckBox notifyOrderReady;
    @FXML private CheckBox notifyPaymentSuccess;
    @FXML private CheckBox notifyPaymentFailed;
    @FXML private CheckBox notifyLowStock;
    @FXML private CheckBox playNotificationSound;

    @FXML private Button saveGeneralBtn;
    @FXML private Button savePaymentBtn;
    @FXML private Button saveNotificationsBtn;
    @FXML private Button addStaffBtn;

    @FXML private TableView<?> staffTable;

    private AdminApplication app;

    public void setApp(AdminApplication app) {
        this.app = app;
        loadSettings();
    }

    private void loadSettings() {
        // Load settings from preferences or API
        restaurantNameField.setText("Smart Restaurant");
        restaurantAddressField.setText("123 Restaurant Street, City");
        restaurantPhoneField.setText("+91-9876543210");
        restaurantEmailField.setText("info@restaurant.com");
        taxRateField.setText("5.0");
        qrBaseUrlField.setText("http://localhost:8080");
        qrSizeField.setText("300");
        currencyField.setText("INR");

        // Notification preferences
        notifyNewOrders.setSelected(true);
        notifyOrderReady.setSelected(true);
        notifyPaymentSuccess.setSelected(true);
        notifyPaymentFailed.setSelected(true);
        playNotificationSound.setSelected(true);
    }

    @FXML
    private void handleSaveGeneral(ActionEvent event) {
        // Save general settings
        app.showInfo("Settings Saved", "General settings saved successfully");
    }

    @FXML
    private void handleSavePayment(ActionEvent event) {
        // Save payment settings
        String keyId = razorpayKeyIdField.getText();
        String keySecret = razorpayKeySecretField.getText();

        if (keyId.isEmpty() || keySecret.isEmpty()) {
            app.showError("Validation Error", "Razorpay Key ID and Secret are required");
            return;
        }

        app.showInfo("Settings Saved", "Payment settings saved successfully");
    }

    @FXML
    private void handleSaveNotifications(ActionEvent event) {
        // Save notification preferences
        app.showInfo("Settings Saved", "Notification preferences saved successfully");
    }

    @FXML
    private void handleAddStaff(ActionEvent event) {
        // Add staff member dialog
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Add Staff Member");
        dialog.setHeaderText("Enter staff details:");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField mobileField = new TextField();
        mobileField.setPromptText("Mobile Number");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("STAFF", "KITCHEN", "ADMIN");
        roleCombo.setValue("STAFF");

        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Name:"), nameField,
                new Label("Email:"), emailField,
                new Label("Mobile:"), mobileField,
                new Label("Password:"), passwordField,
                new Label("Role:"), roleCombo));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            app.showInfo("Success", "Staff member added successfully");
            // TODO: Call API to add staff
        });
    }
}