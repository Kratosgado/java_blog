package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.notifications.Toast;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardHomeController {
  private static final Logger logger = LoggerFactory.getLogger(DashboardHomeController.class);

  @FXML
  private Label totalPostsLabel;

  @FXML
  private Label totalCommentsLabel;

  @FXML
  private Label totalViewsLabel;

  @FXML
  private Label totalTagsLabel;

  @FXML
  private VBox recentPostsContainer;

  private final PostService postService;
  private final CommentService commentService;
  private final TagService tagService;

  public DashboardHomeController() {
    this.postService = new PostService();
    this.commentService = new CommentService();
    this.tagService = new TagService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Dashboard Home Controller");
    loadDashboardData();
  }

  private void loadDashboardData() {
    try {
      int currentUserId = AuthContext.getInstance().getCurrentUser().getId();

      var allPosts = postService.getPostsByUserId(currentUserId);
      totalPostsLabel.setText(String.valueOf(allPosts.size()));

      int totalComments = 0;
      for (Post post : allPosts) {
        totalComments += commentService.getCommentCountForPost(post.getId());
      }
      totalCommentsLabel.setText(String.valueOf(totalComments));

      long totalViews = postService.getTotalViews(currentUserId);
      totalViewsLabel.setText(String.valueOf(totalViews));

      var allTags = tagService.getAllTags();
      totalTagsLabel.setText(String.valueOf(allTags.size()));

      loadRecentPosts(allPosts);

      logger.info("Dashboard data loaded successfully");
    } catch (Exception e) {
      logger.error("Failed to load dashboard data", e);
      Toast.error("Failed to load dashboard data");
    }
  }

  private void loadRecentPosts(java.util.List<Post> allPosts) {
    try {
      recentPostsContainer.getChildren().clear();

      var recentPosts = allPosts.stream()
          .limit(5)
          .toList();

      for (Post post : recentPosts) {
        HBox postItem = createPostItem(post);
        recentPostsContainer.getChildren().add(postItem);
      }
    } catch (Exception e) {
      logger.error("Failed to load recent posts", e);
    }
  }

  private HBox createPostItem(Post post) {
    HBox container = new HBox(15);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setStyle(
        "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

    VBox postInfo = new VBox(5);
    Label titleLabel = new Label(post.getTitle());
    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
    Label statusLabel = new Label("Status: " + post.getStatus().toUpperCase());
    statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
    Label statsLabel = new Label("Views: " + post.getViews() + " | Date: " + post.getCreatedAt());
    statsLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");

    postInfo.getChildren().addAll(titleLabel, statusLabel, statsLabel);

    HBox spacer = new HBox();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    container.getChildren().addAll(postInfo, spacer);
    return container;
  }

  @FXML
  private void viewAllPosts() {
    try {
      DashboardController.instance().getPostsButton().fire();
    } catch (Exception e) {
      logger.error("Failed to navigate to posts", e);
      Toast.error("Failed to navigate to posts");
    }
  }
}
