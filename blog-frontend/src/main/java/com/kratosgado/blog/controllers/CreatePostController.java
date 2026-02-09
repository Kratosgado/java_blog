package com.kratosgado.blog.controllers;

import com.google.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.CategoryService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.services.UploadService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.context.AuthContext;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class CreatePostController {
  private static final Logger logger = LoggerFactory.getLogger(CreatePostController.class);

  @FXML
  private Label headerLabel;

  @FXML
  private TextField titleField;

  @FXML
  private TextArea contentArea;

  @FXML
  private Label wordCountLabel;

  @FXML
  private TextField tagInputField;

  @FXML
  private ComboBox<String> tagComboBox;

  @FXML
  private Button addTagBtn;

  @FXML
  private FlowPane tagsFlowPane;

  @FXML
  private ComboBox<String> categoryComboBox;

  @FXML
  private TextArea excerptArea;

  @FXML
  private TextField coverImageField;

  @FXML
  private Button uploadCoverImageBtn;
  @FXML
  private Label messageLabel;

  @FXML
  private Button saveAsDraftBtn;

  @FXML
  private Button publishBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button previewBtn;

  private final PostService postService;
  private final TagService tagService;
  private final UploadService uploadService;
  private final CategoryService categoryService;
  private List<String> postTags; // Track selected tag names
  private List<Long> postTagIds; // Track selected tag IDs

  @Inject
  public CreatePostController(PostService postService, TagService tagService, UploadService uploadService,
      CategoryService categoryService) {
    this.postService = postService;
    this.tagService = tagService;
    this.uploadService = uploadService;
    this.categoryService = categoryService;
    this.postTags = new ArrayList<>();
    this.postTagIds = new ArrayList<>();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Create Post Controller");
    setupUI();
  }

  private void setupUI() {
    contentArea.textProperty().addListener((obs, oldVal, newVal) -> updateWordCount());

    publishBtn.setOnAction(e -> publishPost());
    saveAsDraftBtn.setOnAction(e -> saveDraft());
    cancelBtn.setOnAction(e -> cancel());
    previewBtn.setOnAction(e -> preview());
    addTagBtn.setOnAction(e -> addTagFromComboBox());
    uploadCoverImageBtn.setOnAction(e -> uploadCoverImage());

    loadCategories();
    loadTags();
  }

  private void loadCategories() {
    try {
      List<Category> categories = categoryService.getAllCategories();
      categoryComboBox.getItems().clear();

      if (categories.isEmpty()) {
        categoryComboBox.getItems().add("Uncategorized");
      } else {
        for (Category category : categories) {
          categoryComboBox.getItems().add(category.getName());
        }
      }

      // Select first item by default
      if (!categoryComboBox.getItems().isEmpty()) {
        categoryComboBox.getSelectionModel().selectFirst();
      }
    } catch (Exception e) {
      logger.error("Failed to load categories", e);
      categoryComboBox.getItems().addAll("Technology", "Lifestyle", "Business", "Travel", "Other");
    }
  }

  private void loadTags() {
    try {
      var tags = tagService.getAllTags();
      if (tagComboBox != null) {
        tagComboBox.getItems().clear();
        for (var tag : tags) {
          tagComboBox.getItems().add(tag.getName());
        }
        if (!tagComboBox.getItems().isEmpty()) {
          tagComboBox.getSelectionModel().selectFirst();
        }
      }
    } catch (Exception e) {
      logger.error("Failed to load tags", e);
    }
  }

  private void updateWordCount() {
    String text = contentArea.getText();
    int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    wordCountLabel.setText(wordCount + " words");
  }

  private CreatePostRequest getPostDto(String status) {
    String title = titleField.getText();
    String content = contentArea.getText();
    String excerpt = excerptArea.getText();
    Long categoryId = getSelectedCategoryId();
    String coverImage = coverImageField.getText();
    
    Long[] tagIds = postTagIds.toArray(new Long[0]);
    return new CreatePostRequest(title, content, excerpt, categoryId, coverImage, status, tagIds);
  }

  private Long getSelectedCategoryId() {
    String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
    if (selectedCategory == null || selectedCategory.isEmpty() || selectedCategory.equals("Uncategorized")) {
      return null;
    }
    try {
      List<Category> categories = categoryService.getAllCategories();
      for (Category category : categories) {
        if (category.getName().equals(selectedCategory)) {
          return category.getId();
        }
      }
    } catch (Exception e) {
      logger.error("Failed to get category ID", e);
    }
    return null;
  }

  private void publishPost() {
    if (validateForm()) {
      try {
        CreatePostRequest dto = getPostDto("published");
        var createdPost = postService.createPost(dto);

        logger.info("Publishing post: {}", createdPost.getTitle());

        messageLabel.setText("Post published successfully!");
        messageLabel.setStyle("-fx-text-fill: #6b7280;");
        clearForm();
        DashboardController.instance().goToPosts();
      } catch (Exception ex) {
        logger.error("Failed to publish post", ex);
        showMessage(ex.getMessage(), "#1f2937");
      }
    }
  }

  private void saveDraft() {
    // Drafts can be saved without category/tags validation
    if (!titleField.getText().isEmpty()) {
      try {
        CreatePostRequest dto = getPostDto("draft");
        var createdPost = postService.createPost(dto);

        logger.info("Saving draft: {}", createdPost.getTitle());

        messageLabel.setText("Draft saved successfully!");
        messageLabel.setStyle("-fx-text-fill: #6b7280;");
        clearForm();
        DashboardController.instance().goToPosts();
      } catch (Exception ex) {
        logger.error("Failed to save draft", ex);
        showMessage(ex.getMessage(), "#1f2937");
      }
    }
  }

  private void cancel() {
    try {
      logger.info("Create post cancelled");
      Navigator.getInstance().popScreen();
    } catch (Exception e) {
      logger.error("Failed to navigate back", e);
    }
  }

  private void preview() {
    logger.info("Previewing post");
    com.kratosgado.blog.utils.DialogUtils.showInfo("Post Preview", 
        "Title: " + titleField.getText() + "\n\n" +
        "Content: " + contentArea.getText().substring(0, Math.min(contentArea.getText().length(), 200)) + "...");
  }

  private void addTagFromComboBox() {
    if (tagComboBox == null || tagComboBox.getValue() == null) {
      return;
    }

    String selectedTagName = tagComboBox.getValue().trim();
    if (selectedTagName.isEmpty() || postTags.contains(selectedTagName)) {
      return;
    }

    // Get the tag ID
    try {
      var tags = tagService.getAllTags();
      for (var tag : tags) {
        if (tag.getName().equals(selectedTagName)) {
          Long tagId = tag.getId();

          // Add to tracking lists
          postTags.add(selectedTagName);
          postTagIds.add(tagId);

          // Create visual tag chip
          HBox tagChip = createTagChip(selectedTagName, tagId);
          tagsFlowPane.getChildren().add(tagChip);

          logger.debug("Added tag: {} (ID: {})", selectedTagName, tagId);
          break;
        }
      }
    } catch (Exception e) {
      logger.error("Failed to add tag", e);
    }
  }

  private HBox createTagChip(String tagName, Long tagId) {
    HBox tagChip = new HBox(5);
    tagChip.setAlignment(Pos.CENTER);
    tagChip.setStyle(
        "-fx-background-color: #6b7280; -fx-padding: 5 10; -fx-background-radius: 15; -fx-text-fill: white;");

    Label tagLabel = new Label(tagName + " ✕");
    tagLabel.setStyle("-fx-text-fill: white; -fx-cursor: hand;");
    tagLabel.setOnMouseClicked(e -> {
      postTags.remove(tagName);
      postTagIds.remove(tagId);
      tagsFlowPane.getChildren().remove(tagChip);
    });

    tagChip.getChildren().add(tagLabel);
    return tagChip;
  }

  private void uploadCoverImage() {
    File selectedFile = uploadService.chooseImageFile(Navigator.getInstance().getStage(), "Select Cover Image");
    if (selectedFile != null) {
      coverImageField.setText(selectedFile.toURI().toString());
      logger.debug("Cover image selected: {}", selectedFile.getName());
    }
  }

  private boolean validateForm() {
    if (titleField.getText().isEmpty()) {
      showMessage("Title is required", "#1f2937");
      return false;
    }
    if (contentArea.getText().isEmpty()) {
      showMessage("Content is required", "#1f2937");
      return false;
    }

    // Category is mandatory for publishing
    String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
    if (selectedCategory == null || selectedCategory.isEmpty() || selectedCategory.equals("Uncategorized")) {
      showMessage("Please select a category", "#1f2937");
      return false;
    }

    // At least one tag is mandatory for publishing
    if (postTagIds.isEmpty()) {
      showMessage("Please add at least one tag", "#1f2937");
      return false;
    }

    return true;
  }

  private void showMessage(String message, String color) {
    messageLabel.setText(message);
    messageLabel.setStyle("-fx-text-fill: " + color + ";");
  }

  private void clearForm() {
    titleField.clear();
    contentArea.clear();
    excerptArea.clear();
    coverImageField.clear();
    if (tagInputField != null) {
      tagInputField.clear();
    }
    tagsFlowPane.getChildren().clear();
    postTags.clear();
    postTagIds.clear();

    // Reset category selection
    if (!categoryComboBox.getItems().isEmpty()) {
      categoryComboBox.getSelectionModel().selectFirst();
    }
  }
}
