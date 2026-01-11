package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.services.CommentService;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CommentsManagementController {
  private static final Logger logger = LoggerFactory.getLogger(CommentsManagementController.class);

  @FXML
  private ComboBox<String> statusFilterCombo;

  @FXML
  private Label totalCommentsLabel;

  @FXML
  private Label pendingCommentsLabel;

  @FXML
  private Label approvedCommentsLabel;

  @FXML
  private TableView<Comment> commentsTable;

  @FXML
  private TableColumn<Comment, String> commentColumn;

  @FXML
  private TableColumn<Comment, String> authorColumn;

  @FXML
  private TableColumn<Comment, String> postColumn;

  @FXML
  private TableColumn<Comment, String> dateColumn;

  @FXML
  private TableColumn<Comment, String> statusColumn;

  @FXML
  private TableColumn<Comment, Void> actionsColumn;

  private final CommentService commentService;
  private ObservableList<Comment> commentsList;

  public CommentsManagementController() {
    this.commentService = new CommentService();
    this.commentsList = FXCollections.observableArrayList();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Comments Management Controller");
    setupUI();
    loadComments();
  }

  private void setupUI() {
    // Setup status filter
    statusFilterCombo.getItems().addAll("All", "Pending", "Approved", "Rejected");
    statusFilterCombo.setValue("All");
    statusFilterCombo.setOnAction(e -> filterComments());

    // Setup table columns
    commentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
    authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
    postColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Post #" + data.getValue().getPostId()));
    dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Active"));

    // Setup actions column with buttons
    actionsColumn.setCellFactory(param -> new TableCell<>() {
      private final Button approveBtn = new Button("Approve");
      private final Button rejectBtn = new Button("Reject");
      private final Button deleteBtn = new Button("Delete");
      private final HBox container = new HBox(8, approveBtn, rejectBtn, deleteBtn);

      {
        container.setAlignment(Pos.CENTER);
        approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 6 12;");
        rejectBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 6 12;");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 6 12;");

        approveBtn.setOnAction(e -> {
          Comment comment = getTableView().getItems().get(getIndex());
          approveComment(comment.getId());
        });

        rejectBtn.setOnAction(e -> {
          Comment comment = getTableView().getItems().get(getIndex());
          rejectComment(comment.getId());
        });

        deleteBtn.setOnAction(e -> {
          Comment comment = getTableView().getItems().get(getIndex());
          deleteComment(comment.getId());
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : container);
      }
    });

    commentsTable.setItems(commentsList);
  }

  private void loadComments() {
    try {
      commentsList.clear();
      var comments = commentService.getAllComments();
      logger.info("Loading {} comments", comments.size());

      commentsList.addAll(comments);
      updateStatistics(comments);
    } catch (Exception e) {
      logger.error("Failed to load comments", e);
    }
  }

  private void updateStatistics(java.util.List<Comment> comments) {
    int total = comments.size();
    // Since Comment model doesn't have status field yet, we'll just show totals
    long pending = 0;
    long approved = total;

    totalCommentsLabel.setText(String.valueOf(total));
    pendingCommentsLabel.setText(String.valueOf(pending));
    approvedCommentsLabel.setText(String.valueOf(approved));
  }

  private void filterComments() {
    String filter = statusFilterCombo.getValue();
    try {
      var allComments = commentService.getAllComments();
      commentsList.clear();

      // For now, show all comments since status field doesn't exist yet
      commentsList.addAll(allComments);
    } catch (Exception e) {
      logger.error("Failed to filter comments", e);
    }
  }

  private void approveComment(int commentId) {
    try {
      if (commentService.approveComment(commentId)) {
        logger.info("Comment approved: {}", commentId);
        // Show success notification
        loadComments();
      }
    } catch (Exception ex) {
      logger.error("Failed to approve comment", ex);
    }
  }

  private void rejectComment(int commentId) {
    try {
      if (commentService.rejectComment(commentId)) {
        logger.info("Comment rejected: {}", commentId);
        // Show success notification
        loadComments();
      }
    } catch (Exception ex) {
      logger.error("Failed to reject comment", ex);
    }
  }

  private void deleteComment(int commentId) {
    try {
      if (com.kratosgado.blog.utils.DialogUtils.showConfirmation(
          "Delete Comment", 
          "Are you sure you want to delete this comment? This action cannot be undone.")) {
        if (commentService.deleteComment(commentId)) {
          logger.info("Comment deleted: {}", commentId);
          loadComments();
        }
      }
    } catch (Exception ex) {
      logger.error("Failed to delete comment", ex);
    }
  }
}
