package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import com.restaurant.admin.model.MenuItem;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    @FXML private TableView<MenuItem> menuItemsTable;
    @FXML private TableColumn<MenuItem, String> colItemName;
    @FXML private TableColumn<MenuItem, String> colItemCategory;
    @FXML private TableColumn<MenuItem, String> colItemPrice;
    @FXML private TableColumn<MenuItem, String> colItemType;
    @FXML private TableColumn<MenuItem, Number> colItemPrepTime;
    @FXML private TableColumn<MenuItem, String> colItemAvailability;
    @FXML private TableColumn<MenuItem, String> colItemFeatured;
    @FXML private TableColumn<MenuItem, Void> colItemActions;

    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField menuSearchField;
    @FXML private Button addMenuItemBtn;

    private AdminApplication app;
    private ApiService apiService;
    private ObservableList<MenuItem> menuItemsData = FXCollections.observableArrayList();
    private List<com.restaurant.admin.model.Category> categories;

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();
        initializeTable();
        loadCategories();
        loadMenuItems();
    }

    private void initializeTable() {
        colItemName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colItemCategory.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCategoryName() != null ? data.getValue().getCategoryName() : ""));
        colItemPrice.setCellValueFactory(data -> {
            BigDecimal price = data.getValue().getPrice();
            return new SimpleStringProperty(price != null ? String.format("₹%,.2f", price) : "₹0.00");
        });
        colItemType.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFoodType() != null ? data.getValue().getFoodType() : ""));
        colItemPrepTime.setCellValueFactory(data -> {
            Integer time = data.getValue().getPreparationTimeMinutes();
            return new javafx.beans.property.SimpleIntegerProperty(time != null ? time : 15);
        });
        colItemAvailability.setCellValueFactory(data -> {
            Boolean available = data.getValue().getIsAvailable();
            return new SimpleStringProperty(available != null && available ? "Available" : "Unavailable");
        });
        colItemFeatured.setCellValueFactory(data -> {
            Boolean featured = data.getValue().getIsFeatured();
            return new SimpleStringProperty(featured != null && featured ? "Yes" : "No");
        });

        colItemActions.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.getStyleClass().add("action-button");
                deleteBtn.getStyleClass().add("action-button-danger");

                editBtn.setOnAction(event -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    handleEditMenuItem(item);
                });

                deleteBtn.setOnAction(event -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    handleDeleteMenuItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5, editBtn, deleteBtn);
                    hbox.setStyle("-fx-alignment: CENTER;");
                    setGraphic(hbox);
                }
            }
        });

        menuItemsTable.setItems(menuItemsData);
    }

    @FXML
    private void handleAddMenuItem(ActionEvent event) {
        showMenuItemDialog(null);
    }

    private void handleEditMenuItem(MenuItem item) {
        showMenuItemDialog(item);
    }

    private void showMenuItemDialog(MenuItem existingItem) {
        Dialog<ApiService.MenuItemRequest> dialog = new Dialog<>();
        dialog.setTitle(existingItem == null ? "Add Menu Item" : "Edit Menu Item");
        dialog.setHeaderText(existingItem == null ? "Enter menu item details:" : "Edit menu item details:");

        // Create form fields
        ComboBox<String> categoryCombo = new ComboBox<>();
        if (categories != null) {
            categories.forEach(cat -> categoryCombo.getItems().add(cat.getName()));
        }
        if (existingItem != null && existingItem.getCategoryName() != null) {
            categoryCombo.setValue(existingItem.getCategoryName());
        }

        TextField nameField = new TextField(existingItem != null ? existingItem.getName() : "");
        TextArea descriptionArea = new TextArea(existingItem != null ? existingItem.getDescription() : "");
        descriptionArea.setPrefHeight(60);
        TextField priceField = new TextField(existingItem != null ? existingItem.getPrice().toString() : "");
        TextField prepTimeField = new TextField(
                existingItem != null ? existingItem.getPreparationTimeMinutes().toString() : "15");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("VEG", "NON_VEG", "VEGAN", "GLUTEN_FREE");
        if (existingItem != null && existingItem.getFoodType() != null) {
            typeCombo.setValue(existingItem.getFoodType());
        }
        CheckBox availableCheck = new CheckBox("Available");
        availableCheck.setSelected(existingItem == null || existingItem.getIsAvailable());
        CheckBox featuredCheck = new CheckBox("Featured");
        featuredCheck.setSelected(existingItem != null && existingItem.getIsFeatured());

        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Category:"), categoryCombo,
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionArea,
                new Label("Price (₹):"), priceField,
                new Label("Preparation Time (min):"), prepTimeField,
                new Label("Food Type:"), typeCombo,
                availableCheck, featuredCheck));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String categoryName = categoryCombo.getValue();
                Long categoryId = categories != null ? categories.stream()
                        .filter(c -> c.getName().equals(categoryName))
                        .findFirst()
                        .map(com.restaurant.admin.model.Category::getId)
                        .orElse(null) : null;

                if (categoryId == null) {
                    app.showError("Validation Error", "Please select a category");
                    return null;
                }

                try {
                    BigDecimal price = new BigDecimal(priceField.getText());
                    ApiService.MenuItemRequest request = new ApiService.MenuItemRequest();
                    request.setCategoryId(categoryId);
                    request.setName(nameField.getText().trim());
                    request.setDescription(descriptionArea.getText().trim());
                    request.setPrice(price);
                    request.setPreparationTimeMinutes(Integer.parseInt(prepTimeField.getText()));
                    request.setFoodType(typeCombo.getValue());
                    request.setIsAvailable(availableCheck.isSelected());
                    request.setIsFeatured(featuredCheck.isSelected());
                    return request;
                } catch (NumberFormatException e) {
                    app.showError("Validation Error", "Invalid price or preparation time");
                    return null;
                }
            }
            return null;
        });

        Optional<ApiService.MenuItemRequest> result = dialog.showAndWait();
        result.ifPresent(request -> {
            setLoading(true);
            CompletableFuture<MenuItem> future = existingItem == null
                    ? apiService.createMenuItem(request)
                    : apiService.updateMenuItem(existingItem.getId(), request);

            future.thenAccept(item -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    if (existingItem == null) {
                        menuItemsData.add(item);
                    } else {
                        int index = menuItemsData.indexOf(existingItem);
                        if (index >= 0) {
                            menuItemsData.set(index, item);
                        }
                    }
                    app.showInfo("Success", existingItem == null ? "Menu item created" : "Menu item updated");
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    setLoading(false);
                    app.showError("Error", "Failed: " + throwable.getMessage());
                });
                return null;
            });
        });
    }

    private void handleDeleteMenuItem(MenuItem item) {
        if (app.showConfirmation("Delete Menu Item", "Are you sure you want to delete: " + item.getName() + "?")) {
            setLoading(true);
            apiService.deleteMenuItem(item.getId())
                    .thenAccept(v -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            menuItemsData.remove(item);
                            app.showInfo("Success", "Menu item deleted");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Error", "Failed to delete: " + throwable.getMessage());
                        });
                        return null;
                    });
        }
    }

    private void loadCategories() {
        apiService.getCategories()
                .thenAccept(cats -> {
                    Platform.runLater(() -> {
                        this.categories = cats;
                        categoryFilter.getItems().clear();
                        categoryFilter.getItems().add("All");
                        cats.forEach(cat -> categoryFilter.getItems().add(cat.getName()));
                        categoryFilter.setValue("All");
                    });
                })
                .exceptionally(throwable -> {
                    log.error("Failed to load categories", throwable);
                    return null;
                });
    }

    private void loadMenuItems() {
        setLoading(true);
        apiService.getMenuItems()
                .thenAccept(items -> {
                    Platform.runLater(() -> {
                        menuItemsData.setAll(items);
                        setLoading(false);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        app.showError("Error", "Failed to load menu items: " + throwable.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
    }

    private void setLoading(boolean loading) {
        menuItemsTable.setDisable(loading);
        addMenuItemBtn.setDisable(loading);
    }
}