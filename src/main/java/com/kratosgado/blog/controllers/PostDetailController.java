package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PostDetailController {
  private static final Logger logger = LoggerFactory.getLogger(PostDetailController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private Label authorLabel;

  @FXML
  private Label dateLabel;

  @FXML
  private Label contentLabel;

  @FXML
  private VBox commentsContainer;

  @FXML
  private MFXButton editBtn;

  @FXML
  private MFXButton deleteBtn;

  @FXML
  private MFXButton backBtn;

  @FXML
  private void initialize() {
    logger.debug("Initializing Post Detail Controller");
    setupUI();
  }

  private void setupUI() {
    editBtn.setOnAction(e -> editPost());
    deleteBtn.setOnAction(e -> deletePost());
    backBtn.setOnAction(e -> goBack());
  }

  public void loadPost(int postId) {
    try {
      logger.info("Loading post with id: {}", postId);
    } catch (Exception e) {
      logger.error("Failed to load post", e);
    }
  }

  private void editPost() {
    logger.info("Editing post");
  }

  private void deletePost() {
    logger.info("Deleting post");
  }

  private void goBack() {
    logger.info("Going back");
  }
}
