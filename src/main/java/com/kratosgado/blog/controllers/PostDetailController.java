package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
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
  private TextArea commentTextArea;

  @FXML
  private MFXButton submitCommentBtn;

  private final PostService postService;
  private final CommentService commentService;
  private Post currentPost;

  public PostDetailController() {
    this.postService = new PostService();
    this.commentService = new CommentService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Post Detail Controller");
    setupUI();
  }

  private void setupUI() {
    editBtn.setOnAction(e -> editPost());
    deleteBtn.setOnAction(e -> deletePost());
    backBtn.setOnAction(e -> goBack());
    if (submitCommentBtn != null) {
      submitCommentBtn.setOnAction(e -> submitComment());
    }
  }

  public void loadPost(int postId) {
    try {
      var post = postService.getPostById(postId);
      if (post.isPresent()) {
        currentPost = post.get();
        postService.incrementViews(postId);
        
        titleLabel.setText(currentPost.getTitle());
        authorLabel.setText("By: Author"); // Would need user service to get actual author
        dateLabel.setText("Published: " + currentPost.getCreatedAt());
        contentLabel.setText(currentPost.getContent());

        loadComments(postId);
        
        // Hide edit/delete if not author
        if (currentPost.getUserId() != AuthContext.getInstance().getCurrentUser().getId()) {
          editBtn.setVisible(false);
          deleteBtn.setVisible(false);
        }
        
        logger.info("Post loaded successfully: {}", postId);
      } else {
        logger.warn("Post not found: {}", postId);
      }
    } catch (Exception e) {
      logger.error("Failed to load post", e);
    }
  }

  private void loadComments(int postId) {
    try {
      commentsContainer.getChildren().clear();
      var comments = commentService.getCommentsByPostId(postId);
      
      for (Comment comment : comments) {
        HBox commentBox = createCommentBox(comment);
        commentsContainer.getChildren().add(commentBox);
      }
      logger.info("Loaded {} comments for post {}", comments.size(), postId);
    } catch (Exception e) {
      logger.error("Failed to load comments", e);
    }
  }

  private HBox createCommentBox(Comment comment) {
    HBox box = new HBox(10);
    box.setAlignment(Pos.TOP_LEFT);
    box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9;");
    
    VBox commentInfo = new VBox(5);
    Label authorLabel = new Label(comment.getAuthorName());
    authorLabel.setStyle("-fx-font-weight: bold;");
    Label dateLabel = new Label(comment.getCreatedAt().toString());
    dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");
    Label contentLabel = new Label(comment.getContent());
    contentLabel.setWrapText(true);
    
    commentInfo.getChildren().addAll(authorLabel, dateLabel, contentLabel);
    box.getChildren().add(commentInfo);
    
    return box;
  }

  private void submitComment() {
    if (commentTextArea != null && currentPost != null) {
      String content = commentTextArea.getText().trim();
      if (!content.isEmpty()) {
        try {
          Comment comment = new Comment(
              currentPost.getId(),
              AuthContext.getInstance().getCurrentUser().getId(),
              content
          );
          if (commentService.createComment(comment)) {
            commentTextArea.clear();
            loadComments(currentPost.getId());
            logger.info("Comment submitted successfully");
          }
        } catch (Exception e) {
          logger.error("Failed to submit comment", e);
        }
      }
    }
  }

  private void editPost() {
    if (currentPost != null) {
      logger.info("Editing post: {}", currentPost.getId());
      // Navigate to edit post
    }
  }

  private void deletePost() {
    if (currentPost != null) {
      try {
        if (postService.deletePost(currentPost.getId())) {
          logger.info("Post deleted: {}", currentPost.getId());
          Navigator.getInstance().popScreen();
        }
      } catch (Exception e) {
        logger.error("Failed to delete post", e);
      }
    }
  }

  private void goBack() {
    try {
      logger.info("Going back");
      Navigator.getInstance().popScreen();
    } catch (Exception e) {
      logger.error("Failed to go back", e);
    }
  }
}
