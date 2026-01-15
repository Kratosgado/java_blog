package com.kratosgado.blog.controllers;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.services.CategoryService;
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
  private final CategoryService categoryService;
  private Post currentPost;

  @Override
  public void initData(Object data) {
    logger.debug("Initializing Post View Controller with data: {}", data);
    loadPostContent((Long) data);
    setupUI();
  }

  @Inject
  public PostViewController(PostService postService, CommentService commentService, TagService tagService,
      CategoryService categoryService) {
    this.postService = postService;
    this.commentService = commentService;
    this.tagService = tagService;
    this.categoryService = categoryService;
  }

  @FXML
  private void initialize() {
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

  private void loadPostContent(Long id) {
    try {
      final var postResponse = postService.getPostById(id);

      // Convert PostResponse to Post
      currentPost = new Post();
      currentPost.setId(postResponse.id());
      currentPost.setUserId(postResponse.authorId());
      currentPost.setCategoryId(postResponse.categoryId());
      currentPost.setTitle(postResponse.title());
      currentPost.setContent(postResponse.content());
      currentPost.setExcerpt(postResponse.excerpt());
      currentPost.setStatus(postResponse.status().name().toLowerCase());
      currentPost.setCreatedAt(postResponse.createdAt());
      currentPost.setUpdatedAt(postResponse.updatedAt());
      currentPost.setViews(Integer.valueOf(postResponse.views()));
      currentPost.setLikesCount(Integer.valueOf(postResponse.likesCount()));
      currentPost.setCoverImage(postResponse.coverImage());
      currentPost.setAuthorName(postResponse.authorName());
      currentPost.setAuthorAvatarUrl(postResponse.authorAvatarUrl());

      logger.debug("Post loaded successfully: {}", id);

      displayPost(currentPost);
      loadComments();
      loadTags();
      updateAuthorActionsVisibility();

    } catch (Exception e) {
      logger.error("Failed to load post: {}", id, e);
      ToastNotification.error("Failed to load post");
      Navigator.getInstance().popScreen();
    }
  }

  private void displayPost(Post post) {
    postTitleLabel.setText(post.getTitle());

    // Fetch and display category name
    if (post.getCategoryId() != null) {
      try {
        Category category = categoryService.getCategoryById(post.getCategoryId());
        categoryLabel.setText(category != null ? category.getName() : "Uncategorized");
      } catch (Exception e) {
        logger.error("Failed to load category", e);
        categoryLabel.setText("Uncategorized");
      }
    } else {
      categoryLabel.setText("Uncategorized");
    }

    authorLabel.setText(post.getAuthorName());
    dateLabel.setText(formatDate(post.getCreatedAt()));
    readTimeLabel.setText(calculateReadTime(post.getContent()));

    viewsLabel.setText("👁️ " + post.getViews() + " views");

    // Update author avatar with fallback
    if (post.getAuthorAvatarUrl() != null && !post.getAuthorAvatarUrl().trim().isEmpty()) {
      authorAvatar.setImage(ImageUtils.loadImageWithFallback(post.getAuthorAvatarUrl()));
      sidebarAuthorAvatar.setImage(ImageUtils.loadImageWithFallback(post.getAuthorAvatarUrl()));
    } else {
      authorAvatar.setImage(ImageUtils.loadDefaultAvatar());
      sidebarAuthorAvatar.setImage(ImageUtils.loadDefaultAvatar());
    }

    // Update sidebar author information
    sidebarAuthorLabel.setText(post.getAuthorName());

    // Calculate total posts and views for author
    // TODO: Implement getTotalViews() and getPostsByUserId() in PostService
    try {
      int totalPosts = 0; // postService.getPostsByUserId(post.getUserId()).size();
      long totalViews = 0; // postService.getTotalViews(post.getUserId());
      sidebarAuthorStats.setText(totalPosts + " posts • " + totalViews + " total views");
    } catch (Exception e) {
      logger.debug("Could not load author stats", e);
      sidebarAuthorStats.setText("Statistics unavailable");
    }

    // Since User model doesn't have bio field, use a default message
    if (authorBioLabel != null) {
      authorBioLabel.setText("Content creator and developer");
    }

    loadFeaturedImage();

    contentLabel.setText(post.getContent());
    contentLabel.setWrapText(true);

    updateCommentCount();
  }

  private void loadFeaturedImage() {
    try {
      featuredImage.setImage(ImageUtils.loadImageWithFallback(currentPost.getCoverImage()));
    } catch (Exception e) {
      logger.debug("Cover image not found, using placeholder");
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

      // Load comments from database
      var comments = commentService.getCommentsByPostId(currentPost.getId());
      logger.info("Loading {} comments for post {}", comments.size(), currentPost.getId());

      if (comments.isEmpty()) {
        // Show "no comments" message
        Label noCommentsLabel = new Label("No comments yet. Be the first to comment!");
        noCommentsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
        commentsContainer.getChildren().add(noCommentsLabel);
      } else {
        for (var comment : comments) {
          VBox commentBox = createCommentCard(comment);
          commentsContainer.getChildren().add(commentBox);
        }
      }

      updateCommentCount();
    } catch (Exception e) {
      logger.error("Failed to load comments", e);
      ToastNotification.error("Failed to load comments");
    }
  }

  private VBox createCommentCard(com.kratosgado.blog.models.Comment comment) {
    VBox commentBox = new VBox(10);
    commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");

    HBox headerBox = new HBox(10);
    headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    // Load comment author avatar
    ImageView avatar = new ImageView();
    if (comment.getAuthorAvatarUrl() != null && !comment.getAuthorAvatarUrl().isEmpty()) {
      avatar.setImage(ImageUtils.loadImageWithFallback(comment.getAuthorAvatarUrl()));
    } else {
      avatar.setImage(ImageUtils.loadDefaultAvatar());
    }
    avatar.setFitWidth(40);
    avatar.setFitHeight(40);
    avatar.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 2);");

    // Clip to circle
    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
    avatar.setClip(clip);

    VBox authorInfo = new VBox(2);
    Label authorName = new Label(comment.getAuthorName() != null ? comment.getAuthorName() : "Anonymous");
    authorName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    Label commentTime = new Label(formatTimeAgo(comment.getCreatedAt()));
    commentTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
    authorInfo.getChildren().addAll(authorName, commentTime);

    headerBox.getChildren().addAll(avatar, authorInfo);

    Label commentContent = new Label(comment.getContent());
    commentContent.setWrapText(true);
    commentContent.setStyle("-fx-font-size: 14px; -fx-line-spacing: 1.4;");

    HBox actionsBox = new HBox(15);
    actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    Button likeBtn = new Button("👍 Like");
    likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 12px; -fx-cursor: hand;");
    likeBtn.setOnMouseEntered(e -> likeBtn
        .setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-cursor: hand;"));
    likeBtn.setOnMouseExited(e -> likeBtn
        .setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 12px; -fx-cursor: hand;"));

    Button replyBtn = new Button("Reply");
    replyBtn
        .setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-cursor: hand;");
    replyBtn.setOnMouseEntered(e -> replyBtn
        .setStyle("-fx-background-color: transparent; -fx-text-fill: #4b5563; -fx-font-size: 12px; -fx-cursor: hand;"));
    replyBtn.setOnMouseExited(e -> replyBtn
        .setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-cursor: hand;"));

    // Check if current user is comment author to show delete button
    if (AuthContext.getInstance().getCurrentUser() != null &&
        comment.getUserId() == AuthContext.getInstance().getCurrentUser().getId()) {
      Button deleteBtn = new Button("Delete");
      deleteBtn.setStyle(
          "-fx-background-color: transparent; -fx-text-fill: #1f2937; -fx-font-size: 12px; -fx-cursor: hand;");
      deleteBtn.setOnAction(e -> deleteComment(comment.getId()));
      actionsBox.getChildren().addAll(likeBtn, replyBtn, deleteBtn);
    } else {
      actionsBox.getChildren().addAll(likeBtn, replyBtn);
    }

    commentBox.getChildren().addAll(headerBox, commentContent, actionsBox);
    return commentBox;
  }

  private void deleteComment(Long commentId) {
    try {
      javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
          javafx.scene.control.Alert.AlertType.CONFIRMATION);
      alert.setTitle("Delete Comment");
      alert.setHeaderText("Are you sure you want to delete this comment?");
      alert.setContentText("This action cannot be undone.");

      alert.showAndWait().ifPresent(response -> {
        if (response == javafx.scene.control.ButtonType.OK) {
          try {
            commentService.deleteComment(commentId);
            logger.info("Comment deleted successfully: {}", commentId);
            ToastNotification.success("Comment deleted successfully!");
            loadComments(); // Reload comments
          } catch (Exception e) {
            logger.error("Failed to delete comment: {}", commentId, e);
            ToastNotification.error("Failed to delete comment");
          }
        }
      });
    } catch (Exception e) {
      logger.error("Failed to delete comment", e);
      ToastNotification.error("Failed to delete comment");
    }
  }

  private String formatTimeAgo(java.time.LocalDateTime dateTime) {
    if (dateTime == null)
      return "Unknown";

    java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
    long seconds = duration.getSeconds();

    if (seconds < 60)
      return "Just now";
    if (seconds < 3600)
      return (seconds / 60) + " minutes ago";
    if (seconds < 86400)
      return (seconds / 3600) + " hours ago";
    if (seconds < 604800)
      return (seconds / 86400) + " days ago";
    if (seconds < 2592000)
      return (seconds / 604800) + " weeks ago";
    if (seconds < 31536000)
      return (seconds / 2592000) + " months ago";
    return (seconds / 31536000) + " years ago";
  }

  private void loadTags() {
    try {
      tagsFlowPane.getChildren().clear();

      // Load tags from database
      var tags = tagService.getTagsByPostId(currentPost.getId());
      logger.info("Loading {} tags for post {}", tags.size(), currentPost.getId());

      if (tags.isEmpty()) {
        Label noTagsLabel = new Label("No tags");
        noTagsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        tagsFlowPane.getChildren().add(noTagsLabel);
      } else {
        for (var tag : tags) {
          Label tagLabel = new Label("#" + tag.getName());
          tagLabel.setStyle(
              "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 12px; -fx-cursor: hand;");
          tagLabel.setOnMouseEntered(e -> tagLabel.setStyle(
              "-fx-background-color: #1976d2; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 12px; -fx-cursor: hand;"));
          tagLabel.setOnMouseExited(e -> tagLabel.setStyle(
              "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 12px; -fx-cursor: hand;"));
          tagLabel.setOnMouseClicked(e -> filterByTag(tag.getName()));
          tagsFlowPane.getChildren().add(tagLabel);
        }
      }
    } catch (Exception e) {
      logger.error("Failed to load tags", e);
      ToastNotification.error("Failed to load tags");
    }
  }

  private void loadRelatedPosts() {
    try {
      relatedPostsContainer.getChildren().clear();

      // Load published posts excluding current post
      var allPosts = postService.getPublishedPosts();
      var relatedPosts = allPosts.stream()
          .filter(p -> p.getId() != currentPost.getId())
          .limit(5)
          .toList();

      if (relatedPosts.isEmpty()) {
        Label noPostsLabel = new Label("No related posts");
        noPostsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-padding: 10;");
        relatedPostsContainer.getChildren().add(noPostsLabel);
      } else {
        for (var post : relatedPosts) {
          VBox relatedPostCard = createRelatedPostCard(post);
          relatedPostsContainer.getChildren().add(relatedPostCard);
        }
      }
    } catch (Exception e) {
      logger.error("Failed to load related posts", e);
    }
  }

  private VBox createRelatedPostCard(Post post) {
    VBox card = new VBox(8);
    card.setStyle("-fx-padding: 10; -fx-cursor: hand; -fx-background-radius: 8;");
    card.setOnMouseEntered(e -> card
        .setStyle("-fx-padding: 10; -fx-cursor: hand; -fx-background-color: #f5f5f5; -fx-background-radius: 8;"));
    card.setOnMouseExited(e -> card.setStyle("-fx-padding: 10; -fx-cursor: hand; -fx-background-radius: 8;"));
    card.setOnMouseClicked(e -> openRelatedPost(post.getId()));

    Label titleLabel = new Label(post.getTitle());
    titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #333; -fx-wrap-text: true;");
    titleLabel.setWrapText(true);
    titleLabel.setMaxWidth(200);

    String readTime = calculateReadTime(post.getContent());
    String timeAgo = formatTimeAgo(post.getCreatedAt());
    Label metaLabel = new Label(readTime + " • " + timeAgo);
    metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

    card.getChildren().addAll(titleLabel, metaLabel);
    return card;
  }

  private void openRelatedPost(Long postId) {
    logger.info("Opening related post: {}", postId);
    try {
      Navigator.getInstance().goTo("post-view", postId);
      logger.debug("Opened related post: {}", postId);
    } catch (Exception e) {
      logger.error("Failed to open related post", e);
      ToastNotification.error("Failed to open post");
    }
  }

  private String calculateReadTime(String content) {
    if (content == null || content.isEmpty()) {
      return "1 min read";
    }

    // Average reading speed: 200-250 words per minute
    int wordCount = content.split("\\s+").length;
    int minutes = Math.max(1, (int) Math.ceil(wordCount / 200.0));

    return minutes + " min read";
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
      // Check if user is logged in
      if (AuthContext.getInstance().getCurrentUser() == null) {
        ToastNotification.error("You must be logged in to like a post");
        return;
      }

      // TODO: Implement like/unlike API endpoints
      // Toggle like button state (UI only for now)
      if (likeBtn.getStyle().contains("#2196f3")) {
        // Already liked, remove like
        likeBtn.setStyle("");
        currentPost.setLikesCount(currentPost.getLikesCount() - 1);
        ToastNotification.info("Like removed (UI only - API not implemented)");
        logger.debug("Post unliked (UI only): {}", currentPost.getId());
      } else {
        // Add like
        likeBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        // Remove dislike if it was disliked
        dislikeBtn.setStyle("");

        // Update local state
        currentPost.setLikesCount(currentPost.getLikesCount() + 1);

        ToastNotification.info("Post liked (UI only - API not implemented)");
        logger.debug("Post liked (UI only): {}", currentPost.getId());
      }
    } catch (Exception e) {
      logger.error("Failed to like post", e);
      ToastNotification.error("Failed to like post: " + e.getMessage());
    }
  }

  private void dislikePost() {
    logger.info("Disliking post: {}", currentPost.getId());
    try {
      // Toggle dislike button state
      if (dislikeBtn.getStyle().contains("#1f2937")) {
        // Already disliked, remove dislike
        dislikeBtn.setStyle("");
        ToastNotification.success("Dislike removed");
      } else {
        // Add dislike
        dislikeBtn.setStyle("-fx-background-color: #1f2937; -fx-text-fill: white;");
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
      // Optionally, you could use ScrollPane scrolling if commentsContainer is in a
      // ScrollPane
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
        followAuthorBtn.setStyle(
            "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;");
        ToastNotification.success("Unfollowed author");
        logger.debug("Unfollowed author: {}", currentPost.getUserId());
      } else {
        // Follow
        followAuthorBtn.setText("Following");
        followAuthorBtn.setStyle(
            "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;");
        ToastNotification.success("Following author!");
        logger.debug("Now following author: {}", currentPost.getUserId());
        // In a real implementation, you would save this to a followers table in the
        // database
        // Example:
        // userService.followUser(AuthContext.getInstance().getCurrentUser().getId(),
        // currentPost.getUserId());
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
      javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
          javafx.scene.control.Alert.AlertType.CONFIRMATION);
      alert.setTitle("Delete Post");
      alert.setHeaderText("Are you sure you want to delete this post?");
      alert.setContentText("This action cannot be undone.");

      alert.showAndWait().ifPresent(response -> {
        if (response == javafx.scene.control.ButtonType.OK) {
          try {
            postService.deletePost(currentPost.getId());
            logger.info("Post deleted successfully: {}", currentPost.getId());
            ToastNotification.success("Post deleted successfully!");
            Navigator.getInstance().popScreen();
          } catch (Exception e) {
            logger.error("Failed to delete post: {}", currentPost.getId(), e);
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
        com.kratosgado.blog.dtos.request.UpdatePostRequest dto = new com.kratosgado.blog.dtos.request.UpdatePostRequest(
            currentPost.getTitle(),
            currentPost.getContent(),
            currentPost.getExcerpt(),
            currentPost.getCategoryId(),
            currentPost.getCoverImage(),
            "draft");

        Post updated = postService.updatePost(currentPost.getId(), dto);
        if (updated != null) {
          publishBtn.setText("Publish");
          publishBtn.setStyle(
              "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
          ToastNotification.success("Post unpublished - saved as draft");
          logger.debug("Post unpublished: {}", currentPost.getId());
        } else {
          logger.error("Failed to unpublish post");
          ToastNotification.error("Failed to unpublish post");
        }
      } else {
        // Publish
        com.kratosgado.blog.dtos.request.UpdatePostRequest dto = new com.kratosgado.blog.dtos.request.UpdatePostRequest(
            currentPost.getTitle(),
            currentPost.getContent(),
            currentPost.getExcerpt(),
            currentPost.getCategoryId(),
            currentPost.getCoverImage(),
            "published");

        Post published = postService.updatePost(currentPost.getId(), dto);
        if (published != null) {
          currentPost.setStatus("published");
          publishBtn.setText("Published");
          publishBtn.setStyle(
              "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
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
    if (content.isEmpty()) {
      ToastNotification.error("Comment cannot be empty");
      return;
    }

    // Check if user is logged in
    if (AuthContext.getInstance().getCurrentUser() == null) {
      ToastNotification.error("You must be logged in to comment");
      return;
    }

    try {
      User user = AuthContext.getInstance().getCurrentUser();
      // Create and save comment to database using DTO
      CreateCommentRequest dto = new CreateCommentRequest(
          currentPost.getId(),
          content);

      Comment created = commentService.createComment(dto);
      if (created != null) {
        logger.info("Comment submitted successfully for post {}", currentPost.getId());
        ToastNotification.success("Comment posted successfully!");
        loadComments(); // Reload comments to show the new one
        clearComment();
      } else {
        logger.error("Failed to create comment");
        ToastNotification.error("Failed to post comment");
      }
    } catch (Exception e) {
      logger.error("Failed to submit comment", e);
      ToastNotification.error("Failed to post comment: " + e.getMessage());
    }
  }

  private void clearComment() {
    commentTextArea.clear();
    notifyRepliesCheck.setSelected(false);
  }

  private void filterByTag(String tagName) {
    logger.info("Filtering by tag: {}", tagName);
    try {
      // Navigate back to home screen
      Navigator.getInstance().popScreen();
      ToastNotification.info("Filtering posts by tag: " + tagName);
    } catch (Exception e) {
      logger.error("Failed to filter by tag", e);
      ToastNotification.error("Failed to filter by tag");
    }
  }

  private String formatDate(java.time.LocalDateTime date) {
    if (date == null)
      return "Unknown date";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    return date.format(formatter);
  }

}
