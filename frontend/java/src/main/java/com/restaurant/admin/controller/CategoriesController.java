package com.restaurant.admin.controller;

import com.restaurant.admin.AdminApplication;
import com.restaurant.admin.model.Category;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CategoriesController {

    private static final Logger log = LoggerFactory.getLogger(CategoriesController.class);

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, String> colCatName;
    @FXML private TableColumn<Category, String> colCatDescription;
    @FXML private TableColumn<Category, Number> colCatDisplayOrder;
    @FXML private TableColumn<Category, Number> colCatItemsCount;
    @FXML private TableColumn<Category, String> colCatStatus;
    @FXML private TableColumn<Category, Void> colCatActions;

    @FXML private TextField categorySearchField;
    @FXML private Button addCategoryBtn;

    private AdminApplication app;
    private ApiService apiService;
    private ObservableList<Category> categoriesData = FXCollections.observableArrayList();

    public void setApp(AdminApplication app) {
        this.app = app;
        this.apiService = app.getApiService();
        initializeTable();
        loadCategories();
    }

    private void initializeTable() {
        colCatName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCatDescription.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDescription() != null ? data.getValue().getDescription() : ""));
        colCatDisplayOrder.setCellValueFactory(data -> {
            Integer order = data.getValue().getDisplayOrder();
            return new javafx.beans.property.SimpleIntegerProperty(order != null ? order : 0);
        });
        colCatItemsCount.setCellValueFactory(data -> {
            Long count = data.getValue().getMenuItemsCount();
            return new javafx.beans.property.SimpleLongProperty(count != null ? count : 0);
        });
        colCatStatus.setCellValueFactory(data -> {
            Boolean active = data.getValue().getIsActive();
            return new SimpleStringProperty(active != null && active ? "Active" : "Inactive");
        });

        colCatActions.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.getStyleClass().add("action-button");
                deleteBtn.getStyleClass().add("action-button-danger");

                editBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleEditCategory(category);
                });

                deleteBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(category);
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

        categoriesTable.setItems(categoriesData);
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter category name:");
        dialog.setContentText("Category Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                app.showError("Validation Error", "Category name cannot be empty");
                return;
            }

            setLoading(true);
            ApiService.CategoryRequest request = new ApiService.CategoryRequest();
            request.setName(name.trim());
            request.setDisplayOrder(categoriesData.size() + 1);
            request.setIsActive(true);

            apiService.createCategory(request)
                    .thenAccept(category -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            categoriesData.add(category);
                            app.showInfo("Success", "Category created successfully");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Error", "Failed to create category: " + throwable.getMessage());
                        });
                        return null;
                    });
        });
    }

    private void handleEditCategory(Category category) {
        TextInputDialog dialog = new TextInputDialog(category.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit category name:");
        dialog.setContentText("Category Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                app.showError("Validation Error", "Category name cannot be empty");
                return;
            }

            setLoading(true);
            ApiService.CategoryRequest request = new ApiService.CategoryRequest();
            request.setName(name.trim());
            request.setDescription(category.getDescription());
            request.setDisplayOrder(category.getDisplayOrder());
            request.setIsActive(category.getIsActive());

            apiService.updateCategory(category.getId(), request)
                    .thenAccept(updatedCategory -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            int index = categoriesData.indexOf(category);
                            if (index >= 0) {
                                categoriesData.set(index, updatedCategory);
                            }
                            app.showInfo("Success", "Category updated successfully");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Error", "Failed to update category: " + throwable.getMessage());
                        });
                        return null;
                    });
        });
    }

    private void handleDeleteCategory(Category category) {
        if (app.showConfirmation("Delete Category", "Are you sure you want to delete category: " + category.getName() + "?")) {
            setLoading(true);
            apiService.deleteCategory(category.getId())
                    .thenAccept(v -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            categoriesData.remove(category);
                            app.showInfo("Success", "Category deleted successfully");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            setLoading(false);
                            app.showError("Error", "Failed to delete category: " + throwable.getMessage());
                        });
                        return null;
                    });
        }
    }

    private void loadCategories() {
        setLoading(true);
        apiService.getCategories()
                .thenAccept(categories -> {
                    Platform.runLater(() -> {
                        categoriesData.setAll(categories);
                        setLoading(false);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        app.showError("Error", "Failed to load categories: " + throwable.getMessage());
                        setLoading(false);
                    });
                    return null;
                });
    }

    private void setLoading(boolean loading) {
        categoriesTable.setDisable(loading);
        addCategoryBtn.setDisable(loading);
    }
}