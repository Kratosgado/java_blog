package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;

import java.time.format.DateTimeFormatter;

public class PostViewController {
  private static final Logger logger = LoggerFactory.getLogger(PostViewController.class);

  @FXML
  private MFXButton backBtn;

  @FXML
  private Label categoryLabel;

  @FXML
  private MFXButton shareBtn;

  @FXML
  private MFXButton bookmarkBtn;

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
  private MFXButton followAuthorBtn;

  @FXML
  private FlowPane tagsFlowPane;

  @FXML
  private ImageView featuredImage;

  @FXML
  private Label contentLabel;

  @FXML
  private MFXButton likeBtn;

  @FXML
  private MFXButton dislikeBtn;

  @FXML
  private MFXButton commentBtn;

  @FXML
  private Label viewsLabel;

  @FXML
  private HBox authorActionsContainer;

  @FXML
  private MFXButton editPostBtn;

  @FXML
  private MFXButton deletePostBtn;

  @FXML
  private MFXButton publishBtn;

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
  private MFXComboBox<String> commentSortComboBox;

  @FXML
  private TextArea commentTextArea;

  @FXML
  private CheckBox notifyRepliesCheck;

  @FXML
  private MFXButton cancelCommentBtn;

  @FXML
  private MFXButton postCommentBtn;

  @FXML
  private VBox commentsContainer;

  private final PostService postService;
  private final CommentService commentService;
  private final TagService tagService;
  private Post currentPost;

  public PostViewController() {
    this.postService = new PostService();
    this.commentService = new CommentService();
    this.tagService = new TagService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Post View Controller");
    setupUI();
    loadPostContent();
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

  private void loadPostContent() {
    loadDemoPost();
  }

  private void loadDemoPost() {
    try {
      // Create demo post data using Lombok-generated constructor
      currentPost = new Post();
      currentPost.setId(1);
      currentPost.setUserId(1);
      currentPost.setTitle("Building a Modern Java Blog Application with JavaFX and MaterialFX");
      currentPost.setContent("In this comprehensive guide, we'll explore how to build a modern blog application using JavaFX and MaterialFX. This project showcases the power of combining traditional Java desktop development with modern UI design principles.\n\n" +
        "Technology Stack\n" +
        "Our blog application leverages several cutting-edge technologies:\n" +
        "- JavaFX 21 - The latest version of Java's UI toolkit\n" +
        "- MaterialFX - Material Design components for JavaFX\n" +
        "- PostgreSQL - Robust database backend\n" +
        "- Lombok - Reducing boilerplate code\n\n" +
        "Key Features\n" +
        "The application includes several advanced features that make it stand out:\n" +
        "1. Responsive Design - The UI adapts to different screen sizes\n" +
        "2. Rich Content Support - Posts can contain formatted text and images\n" +
        "3. Interactive Components - Users can like, comment, and share posts\n\n" +
        "Architecture Overview\n" +
        "The application follows a clean architecture pattern with clear separation of concerns:\n" +
        "- Controllers handle user interactions\n" +
        "- Services contain business logic\n" +
        "- DAOs handle database interactions\n\n" +
        "Conclusion\n" +
        "This blog application demonstrates how modern Java desktop applications can be both functional and visually appealing.");
      currentPost.setExcerpt("Building a modern Java blog application with JavaFX, MaterialFX, and PostgreSQL.");
      currentPost.setStatus("published");
      currentPost.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
      currentPost.setUpdatedAt(java.time.LocalDateTime.now());
      currentPost.setViews(1234);
      currentPost.setFeaturedImage("featured-image.jpg");

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
    
    // Set author info
    authorLabel.setText("John Doe");
    dateLabel.setText(formatDate(post.getCreatedAt()));
    readTimeLabel.setText("5 min read");
    
    // Set view count
    viewsLabel.setText("👁️ " + post.getViews() + " views");
    
    // Load featured image
    loadFeaturedImage();
    
    // Load content in Label
    contentLabel.setText(post.getContent());
    contentLabel.setWrapText(true);
    
    // Update comment count
    updateCommentCount();
  }

  private void loadFeaturedImage() {
    try {
      Image image = new Image("file:src/main/resources/images/featured-post-image.jpg");
      featuredImage.setImage(image);
    } catch (Exception e) {
      logger.debug("Featured image not found, using placeholder");
      try {
        Image placeholder = new Image("file:src/main/resources/images/post-placeholder.jpg");
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
    try {
      avatar.setImage(new Image("file:src/main/resources/images/user-avatar-placeholder.jpg"));
      avatar.setFitWidth(40);
      avatar.setFitHeight(40);
      avatar.setStyle("-fx-background-radius: 20;");
    } catch (Exception e) {
      logger.debug("Comment avatar not found");
    }

    VBox authorInfo = new VBox(2);
    Label authorName = new Label("Commenter " + commentNumber);
    authorName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    Label commentTime = new Label((commentNumber * 2) + " hours ago");
    commentTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
    authorInfo.getChildren().addAll(authorName, commentTime);

    headerBox.getChildren().addAll(avatar, authorInfo);

    Label commentContent = new Label("This is a great post! I really enjoyed reading about the technology stack used in this application. The explanations are clear and examples are helpful.");
    commentContent.setWrapText(true);
    commentContent.setStyle("-fx-font-size: 14px; -fx-line-spacing: 1.4;");

    HBox actionsBox = new HBox(15);
    actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    
    MFXButton likeBtn = new MFXButton("👍 " + (commentNumber * 3));
    likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 12px;");
    
    MFXButton replyBtn = new MFXButton("Reply");
    replyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; -fx-font-size: 12px;");

    actionsBox.getChildren().addAll(likeBtn, replyBtn);

    commentBox.getChildren().addAll(headerBox, commentContent, actionsBox);
    return commentBox;
  }

  private void loadTags() {
    try {
      tagsFlowPane.getChildren().clear();
      
      // Load demo tags
      String[] demoTags = {"Java", "JavaFX", "MaterialFX", "PostgreSQL", "Tutorial", "UI Design"};
      for (String tagName : demoTags) {
        Label tagLabel = new Label("#" + tagName);
        tagLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 12px; -fx-cursor: hand;");
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
    logger.info("Sharing post");
  }

  private void bookmarkPost() {
    logger.info("Bookmarking post");
  }

  private void likePost() {
    logger.info("Liking post");
    currentPost.setViews(currentPost.getViews() + 1);
    viewsLabel.setText("👁️ " + currentPost.getViews() + " views");
  }

  private void dislikePost() {
    logger.info("Disliking post");
  }

  private void scrollToComments() {
    logger.info("Scrolling to comments");
  }

  private void followAuthor() {
    logger.info("Following author");
    followAuthorBtn.setText("Following");
    followAuthorBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;");
  }

  private void editPost() {
    logger.info("Editing post: {}", currentPost.getId());
  }

  private void deletePost() {
    logger.info("Deleting post: {}", currentPost.getId());
  }

  private void publishPost() {
    logger.info("Publishing post: {}", currentPost.getId());
    publishBtn.setText("Published");
    publishBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 10 20;");
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
  }

  private void openRelatedPost(String title) {
    logger.info("Opening related post: {}", title);
  }

  private String formatDate(java.time.LocalDateTime date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    return date.format(formatter);
  }
}