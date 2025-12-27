package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.services.CommentService;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CommentsManagementController {
  private static final Logger logger = LoggerFactory.getLogger(CommentsManagementController.class);

  @FXML
  private VBox commentsContainer;

  @FXML
  private Label totalCommentsLabel;

  private final CommentService commentService;

  public CommentsManagementController() {
    this.commentService = new CommentService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Comments Management Controller");
    setupUI();
    loadComments();
  }

  private void setupUI() {
  }

  private void loadComments() {
    try {
      commentsContainer.getChildren().clear();
      var comments = commentService.getAllComments();
      logger.info("Loading {} comments", comments.size());
      totalCommentsLabel.setText("Total Comments: " + comments.size());

      for (Comment comment : comments) {
        HBox commentItem = createCommentItem(comment);
        commentsContainer.getChildren().add(commentItem);
      }
    } catch (Exception e) {
      logger.error("Failed to load comments", e);
    }
  }

  private HBox createCommentItem(Comment comment) {
    HBox container = new HBox(15);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

    VBox commentInfo = new VBox(8);
    Label authorLabel = new Label("By: " + comment.getAuthorName());
    authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
    Label contentLabel = new Label(comment.getContent());
    contentLabel.setStyle("-fx-font-size: 13; -fx-wrap-text: true;");
    contentLabel.setWrapText(true);
    Label dateLabel = new Label("Posted: " + comment.getCreatedAt());
    dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");

    commentInfo.getChildren().addAll(authorLabel, contentLabel, dateLabel);

    HBox spacer = new HBox();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

    MFXButton deleteBtn = new MFXButton("Delete");
    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 16;");
    deleteBtn.setOnAction(e -> deleteComment(comment.getId()));

    container.getChildren().addAll(commentInfo, spacer, deleteBtn);
    return container;
  }

  private void deleteComment(int commentId) {
    try {
      if (commentService.deleteComment(commentId)) {
        logger.info("Deleting comment: {}", commentId);
        loadComments();
      }
    } catch (Exception ex) {
      logger.error("Failed to delete comment", ex);
    }
  }
}
