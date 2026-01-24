package com.kratosgado.blog.controllers;

import com.google.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.services.CategoryService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.services.UploadService;
import com.kratosgado.blog.utils.DialogUtils;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.interfaces.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class EditPostController implements Initializable {
  private static final Logger logger = LoggerFactory.getLogger(EditPostController.class);

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
  private Button addTagBtn;

  @FXML
  private FlowPane tagsFlowPane;

  @FXML
  private ComboBox<String> categoryComboBox;

  @FXML
  private TextArea excerptArea;

  @FXML
  private TextField imageUrlField;

  @FXML
  private Button uploadImageBtn;

  @FXML
  private TextField coverImageField;

  @FXML
  private Button uploadCoverImageBtn;

  @FXML
  private TextField iconField;

  @FXML
  private Button uploadIconBtn;

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
  private Post currentPost;
  private Integer postId;
  private List<String> postTags;

  @Inject
  public EditPostController(PostService postService, TagService tagService, UploadService uploadService,
      CategoryService categoryService) {
    this.postService = postService;
    this.tagService = tagService;
    this.uploadService = uploadService;
    this.categoryService = categoryService;
    this.postTags = new ArrayList<>();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Edit Post Controller");
    setupUI();
  }

  /**
   * Initialize the controller with the post to edit.
   * This method is called by Navigator when data is passed.
   * 
   * @param data The ID of the post to edit (as Integer or Long)
   */
  @Override
  public void initData(Object data) {
    if (data instanceof Integer) {
      this.postId = (Integer) data;
      loadPost(((Integer) data).longValue());
    } else if (data instanceof Long) {
      this.postId = ((Long) data).intValue();
      loadPost((Long) data);
    } else {
      logger.error("Invalid data type passed to EditPostController: {}",
          data != null ? data.getClass().getName() : "null");
      showMessage("Error: Invalid post ID", "#1f2937");
    }
  }

  private void setupUI() {
    contentArea.textProperty().addListener((obs, oldVal, newVal) -> updateWordCount());

    publishBtn.setOnAction(e -> publishPost());
    saveAsDraftBtn.setOnAction(e -> saveDraft());
    cancelBtn.setOnAction(e -> cancel());
    previewBtn.setOnAction(e -> preview());
    addTagBtn.setOnAction(e -> addTag());
    uploadImageBtn.setOnAction(e -> uploadImage());
    uploadCoverImageBtn.setOnAction(e -> uploadCoverImage());
    uploadIconBtn.setOnAction(e -> uploadIcon());

    loadCategories();
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

  private void loadPost(Long postId) {
    try {
      com.kratosgado.blog.dtos.response.PostResponse postResponse = postService.getPostById(postId);
      // Convert PostResponse to Post for backward compatibility
      currentPost = new Post();
      currentPost.setId(postResponse.id());
      currentPost.setTitle(postResponse.title());
      currentPost.setContent(postResponse.content());
      currentPost.setExcerpt(postResponse.excerpt());
      currentPost.setStatus(postResponse.status());
      currentPost.setCoverImage(postResponse.coverImage());
      currentPost.setUserId(postResponse.authorId());
      currentPost.setCategoryId(postResponse.categoryId());

      // Populate form fields with existing post data
      titleField.setText(currentPost.getTitle());
      contentArea.setText(currentPost.getContent());
      excerptArea.setText(currentPost.getExcerpt() != null ? currentPost.getExcerpt() : "");
      imageUrlField.setText(currentPost.getCoverImage() != null ? currentPost.getCoverImage() : "");

      // Load and select post's category
      loadPostCategory(postId);

      // Load post's tags
      loadPostTags(postId);

      // Update word count
      updateWordCount();

      logger.info("Loaded post for editing: {}", currentPost.getTitle());
    } catch (Exception ex) {
      logger.error("Failed to load post", ex);
      showMessage("Failed to load post: " + ex.getMessage(), "#1f2937");
    }
  }

  private void loadPostCategory(Long postId) {
    try {
      List<Category> postCategories = categoryService.getCategoriesByPostId(postId);
      if (!postCategories.isEmpty()) {
        // Select the first category
        String categoryName = postCategories.get(0).getName();
        categoryComboBox.getSelectionModel().select(categoryName);
        logger.debug("Selected category '{}' for post {}", categoryName, postId);
      }
    } catch (Exception e) {
      logger.error("Failed to load post categories", e);
    }
  }

  private void loadPostTags(Long postId) {
    try {
      List<Tag> tags = tagService.getTagsByPostId(postId);
      postTags.clear();
      tagsFlowPane.getChildren().clear();

      for (Tag tag : tags) {
        String tagName = tag.getName();
        postTags.add(tagName);

        // Create visual tag chip
        HBox tagChip = new HBox(5);
        tagChip.setAlignment(Pos.CENTER);
        tagChip.setStyle(
            "-fx-background-color: #6b7280; -fx-padding: 5 10; -fx-background-radius: 15; -fx-text-fill: white;");

        Label tagLabel = new Label(tagName + " ✕");
        tagLabel.setStyle("-fx-text-fill: white;");

        Button removeBtn = new Button();
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        removeBtn.setOnAction(e -> {
          postTags.remove(tagName);
          tagsFlowPane.getChildren().remove(tagChip);
        });

        tagChip.getChildren().add(tagLabel);
        tagsFlowPane.getChildren().add(tagChip);
      }

      logger.debug("Loaded {} tags for post {}", tags.size(), postId);
    } catch (Exception e) {
      logger.error("Failed to load post tags", e);
    }
  }

  private void updateWordCount() {
    String text = contentArea.getText();
    int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    wordCountLabel.setText(wordCount + " words");
  }

  private void updatePostFromForm(PostStatus status) {
    currentPost.setTitle(titleField.getText());
    currentPost.setContent(contentArea.getText());
    currentPost.setExcerpt(excerptArea.getText());
    currentPost.setStatus(status);
    currentPost.setCoverImage(imageUrlField.getText());
  }

  private void publishPost() {
    if (validateForm()) {
      try {
        updatePostFromForm(PostStatus.published);
        
        // Get category ID from selected category
        Long categoryId = null;
        String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory != null && !selectedCategory.isEmpty() && !selectedCategory.equals("Uncategorized")) {
          List<Category> categories = categoryService.getAllCategories();
          for (Category category : categories) {
            if (category.getName().equals(selectedCategory)) {
              categoryId = category.getId();
              break;
            }
          }
        }
        
        com.kratosgado.blog.dtos.request.UpdatePostRequest dto = new com.kratosgado.blog.dtos.request.UpdatePostRequest(
            currentPost.getTitle(),
            currentPost.getContent(),
            currentPost.getExcerpt(),
            categoryId,
            currentPost.getCoverImage(),
            currentPost.getStatus(),
            null);

        Post updatedPost = postService.updatePost(currentPost.getId(), dto);
        if (updatedPost != null) {
          logger.info("Updated and published post: {}", currentPost.getTitle());

          // Update post category
          updatePostCategory(currentPost.getId());

          // Update post tags
          updatePostTags(currentPost.getId());

          showMessage("Post updated and published successfully!", "#6b7280");

          // Navigate back after a short delay
          new Thread(() -> {
            try {
              Thread.sleep(1000);
              javafx.application.Platform.runLater(() -> {
                DashboardController.instance().goToPosts();
              });
            } catch (InterruptedException e) {
              logger.error("Sleep interrupted", e);
            }
          }).start();
        }
      } catch (Exception ex) {
        logger.error("Failed to update post", ex);
        showMessage(ex.getMessage(), "#1f2937");
      }
    }
  }

  private void saveDraft() {
    if (!titleField.getText().isEmpty()) {
      try {
        updatePostFromForm(PostStatus.draft);
        
        // Get category ID from selected category
        Long categoryId = null;
        String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory != null && !selectedCategory.isEmpty() && !selectedCategory.equals("Uncategorized")) {
          List<Category> categories = categoryService.getAllCategories();
          for (Category category : categories) {
            if (category.getName().equals(selectedCategory)) {
              categoryId = category.getId();
              break;
            }
          }
        }
        
        com.kratosgado.blog.dtos.request.UpdatePostRequest dto = new com.kratosgado.blog.dtos.request.UpdatePostRequest(
            currentPost.getTitle(),
            currentPost.getContent(),
            currentPost.getExcerpt(),
            categoryId,
            currentPost.getCoverImage(),
            currentPost.getStatus(),
            null);

        Post updatedPost = postService.updatePost(currentPost.getId(), dto);
        if (updatedPost != null) {
          logger.info("Saved draft: {}", currentPost.getTitle());

          // Update post category
          updatePostCategory(currentPost.getId());

          // Update post tags
          updatePostTags(currentPost.getId());

          showMessage("Draft saved successfully!", "#4b5563");
        }
      } catch (Exception ex) {
        logger.error("Failed to save draft", ex);
        showMessage(ex.getMessage(), "#1f2937");
      }
    }
  }

  private void updatePostCategory(Long postId) {
    String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
    if (selectedCategory != null && !selectedCategory.isEmpty() && !selectedCategory.equals("Uncategorized")) {
      try {
        // Remove all existing categories
        List<Category> existingCategories = categoryService.getCategoriesByPostId(postId);
        for (Category category : existingCategories) {
          categoryService.removeCategoryFromPost(postId, category.getId());
        }

        // Add new selected category
        List<Category> categories = categoryService.getAllCategories();
        for (Category category : categories) {
          if (category.getName().equals(selectedCategory)) {
            categoryService.addCategoryToPost(postId, category.getId());
            logger.info("Category '{}' updated for post {}", selectedCategory, postId);
            break;
          }
        }
      } catch (Exception e) {
        logger.error("Failed to update post category", e);
        // Don't fail the post update if category update fails
      }
    }
  }

  private void updatePostTags(Long postId) {
    try {
      // Remove all existing tags
      List<Tag> existingTags = tagService.getTagsByPostId(postId);
      for (Tag tag : existingTags) {
        tagService.removeTagFromPost(postId, Long.valueOf(tag.getId()));
      }

      // Add new tags
      for (String tagName : postTags) {
        // Generate slug from tag name
        String slug = tagName.toLowerCase().replaceAll("[^a-z0-9]+", "-");

        // Check if tag already exists
        Tag existingTag = null;
        try {
          existingTag = tagService.getTagBySlug(slug);
        } catch (Exception e) {
          // Tag not found, will be handled below
        }

        Long tagId;
        if (existingTag != null) {
          // Use existing tag
          tagId = Long.valueOf(existingTag.getId());
          logger.debug("Using existing tag: {}", tagName);
        } else {
          // TODO: Implement tag creation via API
          logger.warn("Tag creation not yet implemented via API: {}", tagName);
          continue;
        }

        // Associate tag with post
        tagService.addTagToPost(postId, tagId);
        logger.info("Tag '{}' assigned to post {}", tagName, postId);
      }
    } catch (Exception e) {
      logger.error("Failed to update post tags", e);
      // Don't fail the post update if tag update fails
    }
  }

  private void cancel() {
    try {
      boolean confirmed = DialogUtils.showConfirmation(
          "Cancel Editing",
          "Are you sure you want to cancel? Unsaved changes will be lost.");

      if (confirmed) {
        logger.info("Edit post cancelled");
        Navigator.getInstance().popScreen();
      }
    } catch (Exception e) {
      logger.error("Failed to navigate back", e);
    }
  }

  private void preview() {
    logger.info("Previewing post");
    // TODO: Implement preview functionality
  }

  private void addTag() {
    String tag = tagInputField.getText().trim();
    if (!tag.isEmpty() && !postTags.contains(tag)) {
      logger.debug("Adding tag: {}", tag);

      // Add to tracking list
      postTags.add(tag);

      HBox tagChip = new HBox(5);
      tagChip.setAlignment(Pos.CENTER);
      tagChip.setStyle(
          "-fx-background-color: #6b7280; -fx-padding: 5 10; -fx-background-radius: 15; -fx-text-fill: white;");

      Label tagLabel = new Label(tag + " ✕");
      tagLabel.setStyle("-fx-text-fill: white;");

      Button removeBtn = new Button();
      removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
      removeBtn.setOnAction(e -> {
        postTags.remove(tag);
        tagsFlowPane.getChildren().remove(tagChip);
      });

      tagChip.getChildren().add(tagLabel);
      tagsFlowPane.getChildren().add(tagChip);
      tagInputField.clear();
    }
  }

  private void uploadImage() {
    File selectedFile = uploadService.chooseImageFile(Navigator.getInstance().getStage(), "Select Image File");
    if (selectedFile != null) {
      imageUrlField.setText(selectedFile.toURI().toString());
    }
  }

  private void uploadCoverImage() {
    File selectedFile = uploadService.chooseImageFile(Navigator.getInstance().getStage(), "Select Cover Image");
    if (selectedFile != null) {
      coverImageField.setText(selectedFile.toURI().toString());
      logger.debug("Cover image selected: {}", selectedFile.getName());
    }
  }

  private void uploadIcon() {
    File selectedFile = uploadService.chooseImageFile(Navigator.getInstance().getStage(), "Select Icon");
    if (selectedFile != null) {
      iconField.setText(selectedFile.toURI().toString());
      logger.debug("Icon selected: {}", selectedFile.getName());
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
    if (currentPost == null) {
      showMessage("No post loaded for editing", "#1f2937");
      return false;
    }
    return true;
  }

  private void showMessage(String message, String color) {
    messageLabel.setText(message);
    messageLabel.setStyle("-fx-text-fill: " + color + ";");
  }
}
