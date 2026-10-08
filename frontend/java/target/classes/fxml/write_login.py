content = """<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.CheckBox?>
<?import javafx.scene.control.Hyperlink?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.PasswordField?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.AnchorPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.text.Font?>

<AnchorPane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="600" prefWidth="900" xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.restaurant.admin.controller.LoginController">
    <children>
        <!-- Left Panel - Branding -->
        <VBox alignment="CENTER" prefWidth="450" styleClass="login-left-panel" AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0" AnchorPane.topAnchor="0.0">
            <padding>
                <Insets bottom="50.0" left="50.0" right="50.0" top="50.0" />
            </padding>
            <children>
                <Label styleClass="logo-text" text="🍽️" />
                <Label styleClass="brand-title" text="Smart Restaurant" />
                <Label styleClass="brand-subtitle" text="QR Ordering & Payment System" />
                <VBox alignment="CENTER" fillWidth="false" spacing="20.0" styleClass="feature-list" VBox.vgrow="ALWAYS">
                    <children>
                        <HBox alignment="CENTER_LEFT" spacing="15.0">
                            <children>
                                <Label styleClass="feature-icon" text="📱" />
                                <Label styleClass="feature-text" text="QR Code Ordering" />
                            </children>
                        </HBox>
                        <HBox alignment="CENTER_LEFT" spacing="15.0">
                            <children>
                                <Label styleClass="feature-icon" text="💳" />
                                <Label styleClass="feature-text" text="Razorpay Integration" />
                            </children>
                        </HBox>
                        <HBox alignment="CENTER_LEFT" spacing="15.0">
                            <children>
                                <Label styleClass="feature-icon" text="📊" />
                                <Label styleClass="feature-text" text="Real-time Dashboard" />
                            </children>
                        </HBox>
                        <HBox alignment="CENTER_LEFT" spacing="15.0">
                            <children>
                                <Label styleClass="feature-icon" text="🔔" />
                                <Label styleClass="feature-text" text="Live Notifications" />
                            </children>
                        </HBox>
                        <HBox alignment="CENTER_LEFT" spacing="15.0">
                            <children>
                                <Label styleClass="feature-icon" text="🍽️" />
                                <Label styleClass="feature-text" text="Menu Management" />
                            </children>
                        </HBox>
                        <HBox alignment="CENTER_LEFT" spacing="15.0">
                            <children>
                                <Label styleClass="feature-icon" text="📈" />
                                <Label styleClass="feature-text" text="Analytics & Reports" />
                            </children>
                        </HBox>
                    </children>
                </VBox>
                <Label styleClass="version-label" text="Version 1.0.0" />
            </children>
        </VBox>

        <!-- Right Panel - Login Form -->
        <VBox alignment="CENTER" prefWidth="450" styleClass="login-right-panel" AnchorPane.bottomAnchor="0.0" AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">
            <padding>
                <Insets bottom="50.0" left="50.0" right="50.0" top="50.0" />
            </padding>
            <children>
                <Label styleClass="login-title" text="Welcome Back" />
                <Label styleClass="login-subtitle" text="Sign in to access the admin dashboard" />
                <VBox alignment="CENTER" fillWidth="true" spacing="20.0" styleClass="login-form" VBox.vgrow="ALWAYS">
                    <children>
                        <VBox spacing="8.0">
                            <Label styleClass="field-label" text="Email Address" />
                            <TextField fx:id="emailField" promptText="admin@restaurant.com" styleClass="form-field" />
                        </VBox>
                        <VBox spacing="8.0">
                            <HBox alignment="CENTER_LEFT">
                                <children>
                                    <Label styleClass="field-label" text="Password" />
                                    <Hyperlink fx:id="forgotPasswordLink" styleClass="forgot-link" text="Forgot?" HBox.hgrow="ALWAYS" />
                                </children>
                            </HBox>
                            <PasswordField fx:id="passwordField" promptText="Enter password" styleClass="form-field" />
                        </VBox>
                        <HBox alignment="CENTER_LEFT">
                            <children>
                                <CheckBox fx:id="rememberMeCheck" styleClass="remember-checkbox" text="Remember me" />
                            </children>
                        </HBox>
                        <Button fx:id="loginButton" defaultButton="true" maxWidth="1.7976931348623157E308" onAction="#handleLogin" styleClass="login-button" text="Sign In" />
                        <Label fx:id="errorLabel" styleClass="error-label" text="" visible="false" wrapText="true" />
                    </children>
                </VBox>
                <Label styleClass="footer-text" text="© 2024 Smart Restaurant. All rights reserved." />
            </children>
        </VBox>
    </children>
</AnchorPane>"""

with open(r"C:\Users\fizal\OneDrive\Desktop\MY PROJECTS\RESTURANT ORDERING SYSTEM\restaurant-ordering-system\frontend\java\src\main\resources\fxml\login.fxml", "w", encoding="utf-8") as f:
    f.write(content.replace("&", "&"))
print("Done")