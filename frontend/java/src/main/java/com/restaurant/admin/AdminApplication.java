package com.restaurant.admin;

import com.restaurant.admin.controller.LoginController;
import com.restaurant.admin.service.ApiService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class AdminApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(AdminApplication.class);
    
    public static final String APP_TITLE = "Restaurant Admin Dashboard";
    public static final String API_BASE_URL = "http://localhost:8080";
    
    private Stage primaryStage;
    private ApiService apiService;
    private Scene loginScene;
    private Scene mainScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.apiService = new ApiService(API_BASE_URL);
        
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        try {
            // Load login screen
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loginLoader.load();
            LoginController loginController = loginLoader.getController();
            loginController.setApp(this);
            
            loginScene = new Scene(loginRoot);
            loginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/admin.css")).toExternalForm());
            
            primaryStage.setScene(loginScene);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            // Set application icon
            try {
                primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png"))));
            } catch (Exception e) {
                log.debug("Could not load application icon");
            }
            
        } catch (IOException e) {
            log.error("Failed to load login screen", e);
            showError("Startup Error", "Failed to load login screen: " + e.getMessage());
        }
    }

    public void showMainScreen() {
        try {
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent mainRoot = mainLoader.load();
            
            com.restaurant.admin.controller.MainController mainController = mainLoader.getController();
            mainController.setApp(this);
            
            mainScene = new Scene(mainRoot);
            mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/admin.css")).toExternalForm());
            
            primaryStage.setScene(mainScene);
            primaryStage.centerOnScreen();
            
        } catch (IOException e) {
            log.error("Failed to load main screen", e);
            showError("Navigation Error", "Failed to load main screen: " + e.getMessage());
        }
    }

    public void showLoginScreen() {
        primaryStage.setScene(loginScene);
        primaryStage.centerOnScreen();
        apiService.clearAuthToken();
    }

    public ApiService getApiService() {
        return apiService;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmation(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK;
    }

    public static void main(String[] args) {
        launch(args);
    }
}