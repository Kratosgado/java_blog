package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CommentsManagementController {
  private static final Logger logger = LoggerFactory.getLogger(CommentsManagementController.class);

  @FXML
  private VBox commentsContainer;

  @FXML
  private Label totalCommentsLabel;

  // @FXML
  // private MFXButton approveBtn;

  // @FXML
  // private MFXButton rejectBtn;

  // @FXML
  // private MFXButton deleteBtn;

  @FXML
  private void initialize() {
    logger.debug("Initializing Comments Management Controller");
    setupUI();
    loadComments();
  }

  private void setupUI() {
    // approveBtn.setOnAction(e -> approveComment());
    // rejectBtn.setOnAction(e -> rejectComment());
    // deleteBtn.setOnAction(e -> deleteComment());
  }

  private void loadComments() {
    try {
      logger.info("Loading comments");
    } catch (Exception e) {
      logger.error("Failed to load comments", e);
    }
  }

  private void approveComment() {
    logger.info("Approving comment");
  }

  private void rejectComment() {
    logger.info("Rejecting comment");
  }

  private void deleteComment() {
    logger.info("Deleting comment");
  }
}
