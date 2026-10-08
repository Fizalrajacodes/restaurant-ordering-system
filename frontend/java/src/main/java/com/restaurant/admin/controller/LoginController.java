package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import com.restaurant.admin.model.AuthResponse;
import com.restaurant.admin.util.PreferencesUtil;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;
    @FXML private Label errorLabel;
    @FXML private VBox loginForm;
    @FXML private javafx.scene.control.Button loginButton;

    private AdminApplication app;

    public void setApp(AdminApplication app) {
        this.app = app;
        
        // Check for saved credentials
        String savedEmail = PreferencesUtil.getEmail();
        if (savedEmail != null) {
            emailField.setText(savedEmail);
            rememberMeCheck.setSelected(true);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        setLoading(true);
        hideError();

        app.getApiService().login(email, password)
            .thenAccept(this::onLoginSuccess)
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError(throwable.getMessage());
                });
                return null;
            });
    }

    private void onLoginSuccess(AuthResponse response) {
        Platform.runLater(() -> {
            setLoading(false);
            
            if (response != null && response.getAccessToken() != null) {
                // Save credentials if remember me is checked
                if (rememberMeCheck.isSelected()) {
                    PreferencesUtil.saveEmail(response.getUser().getEmail());
                } else {
                    PreferencesUtil.clearEmail();
                }

                // Set auth token
                app.getApiService().setAuthToken(response.getAccessToken());
                
                log.info("Login successful for user: {}", response.getUser().getEmail());
                
                // Navigate to main screen
                app.showMainScreen();
            } else {
                showError("Login failed: Invalid response from server");
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Signing in..." : "Sign In");
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        rememberMeCheck.setDisable(loading);
    }
}