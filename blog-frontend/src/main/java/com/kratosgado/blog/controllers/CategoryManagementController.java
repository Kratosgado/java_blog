package com.kratosgado.blog.controllers;

import java.time.LocalDateTime;
import java.util.List;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.services.CategoryService;
import com.kratosgado.blog.utils.UiUtils;
import com.kratosgado.blog.utils.widgets.ToastNotification;
import com.kratosgado.blog.utils.widgets.CustomButton;
import com.kratosgado.blog.utils.widgets.CustomButton.ButtonType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CategoryManagementController {
  private static final Logger logger = LoggerFactory.getLogger(CategoryManagementController.class);

  @FXML
  private VBox categoryFormContainer;
  @FXML
  private Label formTitleLabel;
  @FXML
  private TextField categoryNameField;
  @FXML
  private Label slugPreviewLabel;
  @FXML
  private TextArea categoryDescriptionArea;
  @FXML
  private Button createCategoryBtn;
  @FXML
  private Button cancelFormBtn;
  @FXML
  private Button saveCategoryBtn;
  @FXML
  private Label totalCategoriesLabel;
  @FXML
  private Label popularCategoryLabel;
  @FXML
  private TextField searchField;
  @FXML
  private TableView<CategoryResponse> categoriesTable;
  @FXML
  private TableColumn<CategoryResponse, Long> idColumn;
  @FXML
  private TableColumn<CategoryResponse, String> nameColumn;
  @FXML
  private TableColumn<CategoryResponse, String> slugColumn;
  @FXML
  private TableColumn<CategoryResponse, String> descriptionColumn;
  @FXML
  private TableColumn<CategoryResponse, Long> postCountColumn;
  @FXML
  private TableColumn<CategoryResponse, LocalDateTime> createdAtColumn;
  @FXML
  private TableColumn<CategoryResponse, Void> actionsColumn;

  private final CategoryService categoryService;
  private ObservableList<CategoryResponse> categories;
  private Category editingCategory;

  @Inject
  public CategoryManagementController(CategoryService categoryService) {
    this.categoryService = categoryService;
    this.categories = FXCollections.observableArrayList();
  }

  @FXML
  private void initialize() {
    setupTable();
    setupSearchFilter();
    setupNameFieldListener();
    loadCategories();
    updateStatistics();
  }

  private void setupTable() {
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    slugColumn.setCellValueFactory(new PropertyValueFactory<>("slug"));
    descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    postCountColumn.setCellValueFactory(new PropertyValueFactory<>("postCount"));

    createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    createdAtColumn.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(LocalDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText(UiUtils.formatDate(item));
          setAlignment(Pos.CENTER);
        }
      }
    });

    actionsColumn.setCellFactory(column -> new TableCell<CategoryResponse, Void>() {
      private final Button editBtn = new Button("✏️ Edit");
      private final Button deleteBtn = new CustomButton("🗑️ Delete", ButtonType.ERROR, e -> {
        CategoryResponse category = getTableView().getItems().get(getIndex());
        handleDeleteCategory(category);

      });
      private final HBox actionBox = new HBox(10, editBtn, deleteBtn);

      {
        editBtn.getStyleClass().addAll("outline", "small-button");
        actionBox.setAlignment(Pos.CENTER);

        editBtn.setOnAction(e -> {
          CategoryResponse category = getTableView().getItems().get(getIndex());
          handleEditCategory(category);
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : actionBox);
      }
    });

    categoriesTable.setItems(categories);
  }

  private void setupSearchFilter() {
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null || newValue.isEmpty()) {
        categoriesTable.setItems(categories);
      } else {
        ObservableList<CategoryResponse> filtered = categories
            .filtered(category -> category.name().toLowerCase().contains(newValue.toLowerCase()) ||
                (category.description() != null && category.description().toLowerCase().contains(newValue.toLowerCase())) ||
                category.slug().toLowerCase().contains(newValue.toLowerCase()));
        categoriesTable.setItems(filtered);
      }
    });
  }

  private void setupNameFieldListener() {
    categoryNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      String slug = newValue.toLowerCase()
          .replaceAll("[^a-z0-9\\s-]", "")
          .replaceAll("\\s+", "-")
          .replaceAll("-+", "-")
          .trim();
      slugPreviewLabel.setText("Slug: " + slug);
    });
  }

  @FXML
  private void showCreateForm() {
    editingCategory = null;
    formTitleLabel.setText("Create New Category");
    categoryNameField.clear();
    categoryDescriptionArea.clear();
    slugPreviewLabel.setText("Slug: will-be-generated-automatically");
    categoryFormContainer.setManaged(true);
    categoryFormContainer.setVisible(true);
    createCategoryBtn.setDisable(true);
  }

  @FXML
  private void hideCreateForm() {
    categoryFormContainer.setManaged(false);
    categoryFormContainer.setVisible(false);
    createCategoryBtn.setDisable(false);
    editingCategory = null;
  }

  @FXML
  private void handleSaveCategory() {
    String name = categoryNameField.getText().trim();
    String description = categoryDescriptionArea.getText().trim();

    if (name.isEmpty()) {
      ToastNotification.error("Category name is required");
      return;
    }

    if (description.isEmpty()) {
      ToastNotification.error("Category description is required");
      return;
    }

    try {
      if (editingCategory == null) {
        // Create new category
        com.kratosgado.blog.dtos.request.CreateCategoryRequest dto = new com.kratosgado.blog.dtos.request.CreateCategoryRequest(
            name, description);
        Category created = categoryService.createCategory(dto);
        if (created != null) {
          ToastNotification.success("Category created successfully");
          loadCategories();
          updateStatistics();
          hideCreateForm();
        } else {
          ToastNotification.error("Failed to create category");
        }
      } else {
        // Update existing category
        com.kratosgado.blog.dtos.request.UpdateCategoryRequest dto = new com.kratosgado.blog.dtos.request.UpdateCategoryRequest(
            editingCategory.getId(), name, description);
        Category updated = categoryService.updateCategory(editingCategory.getId(), dto);
        if (updated != null) {
          ToastNotification.success("Category updated successfully");
          loadCategories();
          updateStatistics();
          hideCreateForm();
        } else {
          ToastNotification.error("Failed to update category");
        }
      }
    } catch (Exception e) {
      logger.error("Error saving category", e);
      ToastNotification.error("Error: " + e.getMessage());
    }
  }

  private void handleEditCategory(CategoryResponse category) {
    // Convert CategoryResponse to Category for editing
    editingCategory = Category.builder()
        .id(category.id())
        .name(category.name())
        .slug(category.slug())
        .description(category.description())
        .build();
    formTitleLabel.setText("Edit Category");
    categoryNameField.setText(category.name());
    categoryDescriptionArea.setText(category.description());
    slugPreviewLabel.setText("Slug: " + category.slug());
    categoryFormContainer.setManaged(true);
    categoryFormContainer.setVisible(true);
    createCategoryBtn.setDisable(true);
  }

  private void handleDeleteCategory(CategoryResponse category) {
    try {
      categoryService.deleteCategory(category.id());
      ToastNotification.success("Category deleted successfully");
      loadCategories();
      updateStatistics();
    } catch (Exception e) {
      logger.error("Error deleting category", e);
      ToastNotification.error("Error: " + e.getMessage());
    }
  }

  private void loadCategories() {
    try {
      List<CategoryResponse> allCategories = categoryService.getAllCategoriesWithPostCount();
      categories.clear();
      categories.addAll(allCategories);
      logger.info("Loaded {} categories", allCategories.size());
    } catch (Exception e) {
      logger.error("Error loading categories", e);
      ToastNotification.error("Failed to load categories");
    }
  }

  private void updateStatistics() {
    try {
      int total = categoryService.getCategoryCount();
      totalCategoriesLabel.setText(String.valueOf(total));

      // Find most popular category (one with most posts)
      if (!categories.isEmpty()) {
        CategoryResponse mostPopular = categories.stream()
            .max((c1, c2) -> Long.compare(c1.postCount(), c2.postCount()))
            .orElse(null);

        if (mostPopular != null && mostPopular.postCount() > 0) {
          popularCategoryLabel.setText(mostPopular.name() + " (" + mostPopular.postCount() + " posts)");
        } else {
          popularCategoryLabel.setText("-");
        }
      } else {
        popularCategoryLabel.setText("-");
      }
    } catch (Exception e) {
      logger.error("Error updating statistics", e);
    }
  }
}
