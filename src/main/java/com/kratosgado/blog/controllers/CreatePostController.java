package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.CreatePostDto;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
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
  private MFXTextField titleField;

  @FXML
  private TextArea contentArea;

  @FXML
  private Label wordCountLabel;

  @FXML
  private MFXTextField tagInputField;

  @FXML
  private MFXButton addTagBtn;

  @FXML
  private FlowPane tagsFlowPane;

  @FXML
  private MFXComboBox<String> categoryComboBox;

  @FXML
  private TextArea excerptArea;

  @FXML
  private MFXTextField imageUrlField;

  @FXML
  private MFXButton uploadImageBtn;

  @FXML
  private Label messageLabel;

  @FXML
  private MFXButton saveAsDraftBtn;

  @FXML
  private MFXButton publishBtn;

  @FXML
  private MFXButton cancelBtn;

  @FXML
  private MFXButton previewBtn;

  private final PostService postService;
  private final TagService tagService;
  private Post currentPost;

  public CreatePostController() {
    this.postService = new PostService();
    this.tagService = new TagService();
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

    categoryComboBox.getItems().addAll("Technology", "Lifestyle", "Business", "Travel", "Other");
    currentPost = new Post();
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
    return new CreatePostDto(userId, title, content, excerpt, status, featuredImage);

  }

  private void publishPost() {
    if (validateForm()) {
      try {

        CreatePostDto dto = getPostDto("published");
        if (postService.createPost(dto)) {
          logger.info("Publishing post: {}", currentPost.getTitle());
          messageLabel.setText("Post published successfully!");
          messageLabel.setStyle("-fx-text-fill: #4CAF50;");
          clearForm();
          Navigator.getInstance().goTo(Routes.POSTS);
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
        if (postService.createPost(dto)) {
          logger.info("Saving draft: {}", currentPost.getTitle());
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
    if (!tag.isEmpty()) {
      logger.debug("Adding tag: {}", tag);
      HBox tagChip = new HBox(5);
      tagChip.setAlignment(Pos.CENTER);
      tagChip.setStyle(
          "-fx-background-color: #667eea; -fx-padding: 5 10; -fx-background-radius: 15; -fx-text-fill: white;");

      Label tagLabel = new Label(tag + " ✕");
      tagLabel.setStyle("-fx-text-fill: white;");

      MFXButton removeBtn = new MFXButton();
      removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
      removeBtn.setOnAction(e -> tagsFlowPane.getChildren().remove(tagChip));

      tagChip.getChildren().add(tagLabel);
      tagsFlowPane.getChildren().add(tagChip);
      tagInputField.clear();
    }
  }

  private void uploadImage() {
    logger.info("Uploading image");
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
    tagInputField.clear();
    tagsFlowPane.getChildren().clear();
  }
}
