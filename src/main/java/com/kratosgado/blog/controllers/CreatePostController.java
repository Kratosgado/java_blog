package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;

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
  }

  private void updateWordCount() {
    String text = contentArea.getText();
    int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    wordCountLabel.setText(wordCount + " words");
  }

  private void publishPost() {
    if (validateForm()) {
      logger.info("Publishing post: {}", titleField.getText());
      messageLabel.setText("Post published successfully!");
      messageLabel.setStyle("-fx-text-fill: #4CAF50;");
    }
  }

  private void saveDraft() {
    if (!titleField.getText().isEmpty()) {
      logger.info("Saving draft: {}", titleField.getText());
      messageLabel.setText("Draft saved successfully!");
      messageLabel.setStyle("-fx-text-fill: #2196F3;");
    }
  }

  private void cancel() {
    logger.info("Create post cancelled");
  }

  private void preview() {
    logger.info("Previewing post");
  }

  private void addTag() {
    String tag = tagInputField.getText().trim();
    if (!tag.isEmpty()) {
      logger.debug("Adding tag: {}", tag);
      tagInputField.clear();
    }
  }

  private void uploadImage() {
    logger.info("Uploading image");
  }

  private boolean validateForm() {
    if (titleField.getText().isEmpty()) {
      messageLabel.setText("Title is required");
      messageLabel.setStyle("-fx-text-fill: #f44336;");
      return false;
    }
    if (contentArea.getText().isEmpty()) {
      messageLabel.setText("Content is required");
      messageLabel.setStyle("-fx-text-fill: #f44336;");
      return false;
    }
    return true;
  }
}
