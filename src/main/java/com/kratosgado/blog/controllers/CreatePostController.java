package com.kratosgado.blog.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.CreatePostDto;
import com.kratosgado.blog.dtos.request.CreateTagDto;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
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
  private List<String> postTags;

  public CreatePostController() {
    this.postService = new PostService();
    this.tagService = new TagService();
    this.uploadService = new UploadService();
    this.categoryService = new CategoryService();
    this.postTags = new ArrayList<>();
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
    addTagBtn.setOnAction(e -> addTag());
    uploadImageBtn.setOnAction(e -> uploadImage());
    uploadCoverImageBtn.setOnAction(e -> uploadCoverImage());
    uploadIconBtn.setOnAction(e -> uploadIcon());

    loadCategories();
    currentPost = new Post();
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

  private void updateWordCount() {
    String text = contentArea.getText();
    int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    wordCountLabel.setText(wordCount + " words");
  }

  private CreatePostDto getPostDto(String status) {
    int userId = AuthContext.getInstance().getCurrentUser().getId();
    String title = titleField.getText();
    String content = contentArea.getText();
    String excerpt = excerptArea.getText();
    String featuredImage = imageUrlField.getText();
    String coverImage = coverImageField.getText();
    String icon = iconField.getText();
    return new CreatePostDto(userId, title, content, excerpt, status, featuredImage, coverImage, icon);

  }

  private void publishPost() {
    if (validateForm()) {
      try {
        CreatePostDto dto = getPostDto("published");
        var postOpt = postService.createPost(dto);
        
        if (postOpt.isPresent()) {
          Post createdPost = postOpt.get();
          logger.info("Publishing post: {}", createdPost.getTitle());
          
          // Assign selected category to post
          assignCategoryToPost(createdPost.getId());
          
          // Assign tags to post
          assignTagsToPost(createdPost.getId());
          
          messageLabel.setText("Post published successfully!");
          messageLabel.setStyle("-fx-text-fill: #4CAF50;");
          clearForm();
          DashboardController.instance().goToPosts();
        }
      } catch (Exception ex) {
        logger.error("Failed to publish post", ex);
        showMessage(ex.getMessage(), "#f44336");
      }
    }
  }

  private void saveDraft() {
    if (!titleField.getText().isEmpty()) {
      try {
        CreatePostDto dto = getPostDto("draft");
        var postOpt = postService.createPost(dto);
        
        if (postOpt.isPresent()) {
          Post createdPost = postOpt.get();
          logger.info("Saving draft: {}", createdPost.getTitle());
          
          // Assign selected category to post
          assignCategoryToPost(createdPost.getId());
          
          // Assign tags to post
          assignTagsToPost(createdPost.getId());
          
          messageLabel.setText("Draft saved successfully!");
          messageLabel.setStyle("-fx-text-fill: #2196F3;");
          clearForm();
        }
      } catch (Exception ex) {
        logger.error("Failed to save draft", ex);
        showMessage(ex.getMessage(), "#f44336");
      }
    }
  }

  private void assignCategoryToPost(int postId) {
    String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
    if (selectedCategory != null && !selectedCategory.isEmpty() && !selectedCategory.equals("Uncategorized")) {
      try {
        List<Category> categories = categoryService.getAllCategories();
        for (Category category : categories) {
          if (category.getName().equals(selectedCategory)) {
            categoryService.addCategoryToPost(postId, category.getId());
            logger.info("Category '{}' assigned to post {}", selectedCategory, postId);
            break;
          }
        }
      } catch (Exception e) {
        logger.error("Failed to assign category to post", e);
        // Don't fail the post creation if category assignment fails
      }
    }
  }

  private void assignTagsToPost(int postId) {
    if (postTags.isEmpty()) {
      return;
    }

    try {
      for (String tagName : postTags) {
        // Generate slug from tag name
        String slug = tagName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        
        // Check if tag already exists
        var existingTag = tagService.getTagBySlug(slug);
        
        int tagId;
        if (existingTag.isPresent()) {
          // Use existing tag
          tagId = existingTag.get().getId();
          logger.debug("Using existing tag: {}", tagName);
        } else {
          // Create new tag
          CreateTagDto tagDto = new CreateTagDto(tagName, null);
          if (tagService.createTag(tagDto)) {
            var newTag = tagService.getTagBySlug(slug);
            if (newTag.isPresent()) {
              tagId = newTag.get().getId();
              logger.info("Created new tag: {}", tagName);
            } else {
              logger.error("Failed to retrieve newly created tag: {}", tagName);
              continue;
            }
          } else {
            logger.error("Failed to create tag: {}", tagName);
            continue;
          }
        }
        
        // Associate tag with post
        tagService.addTagToPost(postId, tagId);
        logger.info("Tag '{}' assigned to post {}", tagName, postId);
      }
    } catch (Exception e) {
      logger.error("Failed to assign tags to post", e);
      // Don't fail the post creation if tag assignment fails
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
          "-fx-background-color: #667eea; -fx-padding: 5 10; -fx-background-radius: 15; -fx-text-fill: white;");

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
      showMessage("Title is required", "#f44336");
      return false;
    }
    if (contentArea.getText().isEmpty()) {
      showMessage("Content is required", "#f44336");
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
    imageUrlField.clear();
    coverImageField.clear();
    iconField.clear();
    tagInputField.clear();
    tagsFlowPane.getChildren().clear();
    postTags.clear();
    
    // Reset category selection
    if (!categoryComboBox.getItems().isEmpty()) {
      categoryComboBox.getSelectionModel().selectFirst();
    }
  }
}
