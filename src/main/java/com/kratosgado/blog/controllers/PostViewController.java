package com.kratosgado.blog.controllers;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.utils.ImageUtils;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.interfaces.Initializable;
import com.kratosgado.blog.utils.notifications.ToastNotification;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PostViewController implements Initializable {
  private static final Logger logger = LoggerFactory.getLogger(PostViewController.class);

  @FXML
  private Button backBtn;

  @FXML
  private Label categoryLabel;

  @FXML
  private Button shareBtn;

  @FXML
  private Button bookmarkBtn;

  @FXML
  private Label postTitleLabel;

  @FXML
  private ImageView authorAvatar;

  @FXML
  private Label authorLabel;

  @FXML
  private Label dateLabel;

  @FXML
  private Label readTimeLabel;

  @FXML
  private Button followAuthorBtn;

  @FXML
  private FlowPane tagsFlowPane;

  @FXML
  private ImageView featuredImage;

  @FXML
  private Label contentLabel;

  @FXML
  private Button likeBtn;

  @FXML
  private Button dislikeBtn;

  @FXML
  private Button commentBtn;

  @FXML
  private Label viewsLabel;

  @FXML
  private HBox authorActionsContainer;

  @FXML
  private Button editPostBtn;

  @FXML
  private Button deletePostBtn;

  @FXML
  private Button publishBtn;

  @FXML
  private VBox relatedPostsContainer;

  @FXML
  private ImageView sidebarAuthorAvatar;

  @FXML
  private Label sidebarAuthorLabel;

  @FXML
  private Label sidebarAuthorStats;

  @FXML
  private Label authorBioLabel;

  @FXML
  private Label commentsCountLabel;

  @FXML
  private ComboBox<String> commentSortComboBox;

  @FXML
  private TextArea commentTextArea;

  @FXML
  private CheckBox notifyRepliesCheck;

  @FXML
  private Button cancelCommentBtn;

  @FXML
  private Button postCommentBtn;

  @FXML
  private VBox commentsContainer;

  private final PostService postService;
  private final CommentService commentService;
  private final TagService tagService;
  private Post currentPost;

  @Override
  public void initData(Object data) {
    logger.debug("Initializing Post View Controller with data: {}", data);
    loadPostContent((int) data);
  }

  public PostViewController() {
    this.postService = new PostService();
    this.commentService = new CommentService();
    this.tagService = new TagService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Post View Controller");
    logger.debug("Initializing Post View Controller");
    setupUI();
  }

  private void setupUI() {
    setupNavigation();
    setupEngagement();
    setupAuthorActions();
    setupComments();
    setupSidebar();
  }

  private void setupNavigation() {
    backBtn.setOnAction(e -> goBack());
    shareBtn.setOnAction(e -> sharePost());
    bookmarkBtn.setOnAction(e -> bookmarkPost());
  }

  private void setupEngagement() {
    likeBtn.setOnAction(e -> likePost());
    dislikeBtn.setOnAction(e -> dislikePost());
    commentBtn.setOnAction(e -> scrollToComments());
    followAuthorBtn.setOnAction(e -> followAuthor());
  }

  private void setupAuthorActions() {
    editPostBtn.setOnAction(e -> editPost());
    deletePostBtn.setOnAction(e -> deletePost());
    publishBtn.setOnAction(e -> publishPost());
  }

  private void setupComments() {
    postCommentBtn.setOnAction(e -> submitComment());
    cancelCommentBtn.setOnAction(e -> clearComment());
    commentSortComboBox.getItems().addAll("Newest", "Oldest", "Most Liked");
    commentSortComboBox.getSelectionModel().selectFirst();
  }

  private void setupSidebar() {
    loadRelatedPosts();
  }

  private void loadPostContent(int id) {
    try {
      final Optional<Post> post = postService.getPostById(id);
      logger.debug("Post loaded successfully: {}", id);
      if (post.isEmpty()) {
        logger.error("Failed to load post: {}", id);
        ToastNotification.error("Failed to load post");
        Navigator.getInstance().popScreen();
        return;
      }
      currentPost = post.get();

      displayPost(currentPost);
      loadComments();
      loadTags();
      updateAuthorActionsVisibility();

    } catch (Exception e) {
      logger.error("Failed to load demo post", e);
    }
  }

  private void displayPost(Post post) {
    postTitleLabel.setText(post.getTitle());

    authorLabel.setText(post.getAuthorName());
    dateLabel.setText(formatDate(post.getCreatedAt()));
    readTimeLabel.setText("5 min read");

    viewsLabel.setText("👁️ " + post.getViews() + " views");

    // Update author avatar with fallback
    if (post.getAuthorAvatarUrl() != null && !post.getAuthorAvatarUrl().trim().isEmpty()) {
      authorAvatar.setImage(ImageUtils.loadImageWithFallback(post.getAuthorAvatarUrl()));
    } else {
      authorAvatar.setImage(ImageUtils.loadDefaultAvatar());
    }

    // Update sidebar author information
    sidebarAuthorLabel.setText(post.getAuthorName());
    sidebarAuthorStats.setText("• " + postService.getTotalViews(post.getUserId()) + " total views");

    loadFeaturedImage();

    contentLabel.setText(post.getContent());
    contentLabel.setWrapText(true);

    updateCommentCount();
  }

  private void loadFeaturedImage() {
    try {
      Image image = new Image(currentPost.getFeaturedImage());
      featuredImage.setImage(image);
    } catch (Exception e) {
      logger.debug("Featured image not found, using placeholder");
      try {
        Image placeholder = new Image("file:src/main/resources/images/java_blog_logo.jpg");
        featuredImage.setImage(placeholder);
      } catch (Exception ex) {
        logger.debug("Placeholder image not found");
      }
    }
  }

  private void loadComments() {
    try {
      commentsContainer.getChildren().clear();

      // Load demo comments
      for (int i = 1; i <= 5; i++) {
        VBox commentBox = createDemoComment(i);
        commentsContainer.getChildren().add(commentBox);
      }

      updateCommentCount();
    } catch (Exception e) {
      logger.error("Failed to load comments", e);
    }
  }

  private VBox createDemoComment(int commentNumber) {
    VBox commentBox = new VBox(10);
    commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");

    HBox headerBox = new HBox(10);
    headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    ImageView avatar = new ImageView();
    avatar.setImage(ImageUtils.loadDefaultAvatar());
    avatar.setFitWidth(40);
    avatar.setFitHeight(40);
    avatar.setStyle("-fx-background-radius: 20;");

    VBox authorInfo = new VBox(2);
    Label authorName = new Label("Commenter " + commentNumber);
    authorName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    Label commentTime = new Label((commentNumber * 2) + " hours ago");
    commentTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
    authorInfo.getChildren().addAll(authorName, commentTime);

    headerBox.getChildren().addAll(avatar, authorInfo);

    Label commentContent = new Label(
        "This is a great post! I really enjoyed reading about the technology stack used in this application. The explanations are clear and examples are helpful.");
    commentContent.setWrapText(true);
    commentContent.setStyle("-fx-font-size: 14px; -fx-line-spacing: 1.4;");

    HBox actionsBox = new HBox(15);
    actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    Button likeBtn = new Button("👍 " + (commentNumber * 3));
    likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 12px;");

    Button replyBtn = new Button("Reply");
    replyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; -fx-font-size: 12px;");

    actionsBox.getChildren().addAll(likeBtn, replyBtn);

    commentBox.getChildren().addAll(headerBox, commentContent, actionsBox);
    return commentBox;
  }

  private void loadTags() {
    try {
      tagsFlowPane.getChildren().clear();

      // Load demo tags
      String[] demoTags = { "Java", "JavaFX", "MaterialFX", "PostgreSQL", "Tutorial", "UI Design" };
      for (String tagName : demoTags) {
        Label tagLabel = new Label("#" + tagName);
        tagLabel.setStyle(
            "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 12px; -fx-cursor: hand;");
        tagLabel.setOnMouseClicked(e -> filterByTag(tagName));
        tagsFlowPane.getChildren().add(tagLabel);
      }
    } catch (Exception e) {
      logger.error("Failed to load tags", e);
    }
  }

  private void loadRelatedPosts() {
    try {
      relatedPostsContainer.getChildren().clear();

      // Load demo related posts
      String[] relatedTitles = {
          "Getting Started with JavaFX 21",
          "Material Design Principles in Desktop Apps",
          "Database Integration Best Practices"
      };

      for (String title : relatedTitles) {
        VBox relatedPostCard = createRelatedPostCard(title);
        relatedPostsContainer.getChildren().add(relatedPostCard);
      }
    } catch (Exception e) {
      logger.error("Failed to load related posts", e);
    }
  }

  private VBox createRelatedPostCard(String title) {
    VBox card = new VBox(8);
    card.setStyle("-fx-padding: 10; -fx-cursor: hand;");
    card.setOnMouseClicked(e -> openRelatedPost(title));

    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #333; -fx-wrap-text: true;");
    titleLabel.setWrapText(true);

    Label metaLabel = new Label("3 min read • 2 days ago");
    metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

    card.getChildren().addAll(titleLabel, metaLabel);
    return card;
  }

  private void updateAuthorActionsVisibility() {
    boolean isAuthor = AuthContext.getInstance().getCurrentUser() != null &&
        currentPost.getUserId() == AuthContext.getInstance().getCurrentUser().getId();

    authorActionsContainer.setVisible(isAuthor);
    authorActionsContainer.setManaged(isAuthor);
  }

  private void updateCommentCount() {
    int commentCount = commentsContainer.getChildren().size();
    commentsCountLabel.setText("(" + commentCount + ")");
  }

  private void goBack() {
    Navigator.getInstance().popScreen();
  }

  private void sharePost() {
    logger.info("Sharing post: {}", currentPost.getId());
    try {
      // Copy post URL to clipboard
      String postUrl = "https://blog.example.com/post/" + currentPost.getId();
      javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
      javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
      content.putString(postUrl);
      clipboard.setContent(content);
      
      ToastNotification.success("Post URL copied to clipboard!");
      logger.debug("Post URL copied: {}", postUrl);
    } catch (Exception e) {
      logger.error("Failed to share post", e);
      ToastNotification.error("Failed to share post");
    }
  }

  private void bookmarkPost() {
    logger.info("Bookmarking post: {}", currentPost.getId());
    try {
      // Toggle bookmark button state
      if (bookmarkBtn.getText().equals("🔖")) {
        bookmarkBtn.setText("✅");
        bookmarkBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        ToastNotification.success("Post bookmarked!");
      } else {
        bookmarkBtn.setText("🔖");
        bookmarkBtn.setStyle("");
        ToastNotification.success("Bookmark removed!");
      }
      logger.debug("Bookmark toggled for post: {}", currentPost.getId());
    } catch (Exception e) {
      logger.error("Failed to bookmark post", e);
      ToastNotification.error("Failed to bookmark post");
    }
  }

  private void likePost() {
    logger.info("Liking post: {}", currentPost.getId());
    try {
      // Toggle like button state
      if (likeBtn.getStyle().contains("#2196f3")) {
        // Already liked, remove like
        likeBtn.setStyle("");
        ToastNotification.success("Like removed");
      } else {
        // Add like and increment views
        likeBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        // Remove dislike if it was disliked
        dislikeBtn.setStyle("");
        
        // Increment views in the database
        currentPost.setViews(currentPost.getViews() + 1);
        boolean updated = postService.incrementViews(currentPost.getId());
        if (updated) {
          viewsLabel.setText("👁️ " + currentPost.getViews() + " views");
          ToastNotification.success("Post liked!");
          logger.debug("Post liked and views incremented: {}", currentPost.getId());
        } else {
          logger.warn("Failed to persist view count");
        }
      }
    } catch (Exception e) {
      logger.error("Failed to like post", e);
      ToastNotification.error("Failed to like post");
    }
  }

  private void dislikePost() {
    logger.info("Disliking post: {}", currentPost.getId());
    try {
      // Toggle dislike button state
      if (dislikeBtn.getStyle().contains("#f44336")) {
        // Already disliked, remove dislike
        dislikeBtn.setStyle("");
        ToastNotification.success("Dislike removed");
      } else {
        // Add dislike
        dislikeBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        // Remove like if it was liked
        likeBtn.setStyle("");
        ToastNotification.success("Post disliked");
      }
      logger.debug("Dislike toggled for post: {}", currentPost.getId());
    } catch (Exception e) {
      logger.error("Failed to dislike post", e);
      ToastNotification.error("Failed to dislike post");
    }
  }

  private void scrollToComments() {
    logger.info("Scrolling to comments section");
    try {
      // Request focus on comments section
      commentsContainer.requestFocus();
      // Optionally, you could use ScrollPane scrolling if commentsContainer is in a ScrollPane
      logger.debug("Scrolled to comments section");
      ToastNotification.info("Scrolled to comments");
    } catch (Exception e) {
      logger.error("Failed to scroll to comments", e);
    }
  }

  private void followAuthor() {
    logger.info("Following author: {}", currentPost.getUserId());
    try {
      // Toggle follow state
      if (followAuthorBtn.getText().equals("Following")) {
        // Unfollow
        followAuthorBtn.setText("Follow");
        followAuthorBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;");
        ToastNotification.success("Unfollowed author");
        logger.debug("Unfollowed author: {}", currentPost.getUserId());
      } else {
        // Follow
        followAuthorBtn.setText("Following");
        followAuthorBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;");
        ToastNotification.success("Following author!");
        logger.debug("Now following author: {}", currentPost.getUserId());
        // In a real implementation, you would save this to a followers table in the database
        // Example: userService.followUser(AuthContext.getInstance().getCurrentUser().getId(), currentPost.getUserId());
      }
    } catch (Exception e) {
      logger.error("Failed to follow/unfollow author", e);
      ToastNotification.error("Failed to update follow status");
    }
  }

  private void editPost() {
    logger.info("Editing post: {}", currentPost.getId());
    try {
      // Navigate to create-post screen with post data for editing
      Navigator.getInstance().goTo("create-post", currentPost);
      logger.debug("Navigated to edit post screen");
    } catch (Exception e) {
      logger.error("Failed to navigate to edit post screen", e);
      ToastNotification.error("Failed to open edit screen");
    }
  }

  private void deletePost() {
    logger.info("Deleting post: {}", currentPost.getId());
    try {
      // Confirm deletion with user
      javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
      alert.setTitle("Delete Post");
      alert.setHeaderText("Are you sure you want to delete this post?");
      alert.setContentText("This action cannot be undone.");
      
      alert.showAndWait().ifPresent(response -> {
        if (response == javafx.scene.control.ButtonType.OK) {
          boolean deleted = postService.deletePost(currentPost.getId());
          if (deleted) {
            logger.info("Post deleted successfully: {}", currentPost.getId());
            ToastNotification.success("Post deleted successfully!");
            Navigator.getInstance().popScreen();
          } else {
            logger.error("Failed to delete post: {}", currentPost.getId());
            ToastNotification.error("Failed to delete post");
          }
        }
      });
    } catch (Exception e) {
      logger.error("Failed to delete post", e);
      ToastNotification.error("Failed to delete post");
    }
  }

  private void publishPost() {
    logger.info("Publishing post: {}", currentPost.getId());
    try {
      // Toggle publish/unpublish
      if (currentPost.getStatus().equals("published")) {
        // Unpublish
        currentPost.setStatus("draft");
        boolean updated = postService.updatePost(currentPost);
        if (updated) {
          publishBtn.setText("Publish");
          publishBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
          ToastNotification.success("Post unpublished - saved as draft");
          logger.debug("Post unpublished: {}", currentPost.getId());
        } else {
          logger.error("Failed to unpublish post");
          ToastNotification.error("Failed to unpublish post");
        }
      } else {
        // Publish
        boolean published = postService.publishPost(currentPost.getId());
        if (published) {
          currentPost.setStatus("published");
          publishBtn.setText("Published");
          publishBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
          ToastNotification.success("Post published successfully!");
          logger.debug("Post published: {}", currentPost.getId());
        } else {
          logger.error("Failed to publish post");
          ToastNotification.error("Failed to publish post");
        }
      }
    } catch (Exception e) {
      logger.error("Failed to toggle publish status", e);
      ToastNotification.error("Failed to update post status");
    }
  }

  private void submitComment() {
    String content = commentTextArea.getText().trim();
    if (!content.isEmpty()) {
      try {
        VBox newComment = createNewComment(content);
        commentsContainer.getChildren().add(0, newComment);
        updateCommentCount();
        clearComment();
        logger.info("Comment submitted successfully");
      } catch (Exception e) {
        logger.error("Failed to submit comment", e);
      }
    }
  }

  private VBox createNewComment(String content) {
    VBox commentBox = new VBox(10);
    commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");

    HBox headerBox = new HBox(10);
    headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    Label authorName = new Label("You");
    authorName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2196f3;");
    Label commentTime = new Label("Just now");
    commentTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

    headerBox.getChildren().addAll(authorName, commentTime);

    Label commentContent = new Label(content);
    commentContent.setWrapText(true);
    commentContent.setStyle("-fx-font-size: 14px; -fx-line-spacing: 1.4;");

    commentBox.getChildren().addAll(headerBox, commentContent);
    return commentBox;
  }

  private void clearComment() {
    commentTextArea.clear();
    notifyRepliesCheck.setSelected(false);
  }

  private void filterByTag(String tagName) {
    logger.info("Filtering by tag: {}", tagName);
    try {
      // Navigate back to home and filter by this tag
      Navigator.getInstance().popScreen(); // Go back to previous screen
      // You could also pass the tag as data to home screen for filtering
      logger.debug("Navigated back to filter by tag: {}", tagName);
      ToastNotification.info("Filtering posts by tag: " + tagName);
    } catch (Exception e) {
      logger.error("Failed to filter by tag", e);
      ToastNotification.error("Failed to filter by tag");
    }
  }

  private void openRelatedPost(String title) {
    logger.info("Opening related post: {}", title);
    try {
      // In a real implementation, you would look up the post by title
      // For now, just show a notification
      ToastNotification.info("Opening post: " + title);
      // Example: Navigator.getInstance().goTo("post-view", postId);
      logger.debug("Opened related post: {}", title);
    } catch (Exception e) {
      logger.error("Failed to open related post", e);
      ToastNotification.error("Failed to open post");
    }
  }

  private String formatDate(java.time.LocalDateTime date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    return date.format(formatter);
  }

}
