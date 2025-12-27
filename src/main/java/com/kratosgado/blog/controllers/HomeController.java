package com.kratosgado.blog.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeController {
  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  @FXML
  private MFXTextField searchField;

  @FXML
  private MFXComboBox<String> categoryComboBox;

  @FXML
  private MFXComboBox<String> sortComboBox;

  @FXML
  private ScrollPane featuredScrollPane;

  @FXML
  private HBox featuredPostsContainer;

  @FXML
  private Label postsCountLabel;

  @FXML
  private MFXToggleButton gridViewBtn;

  @FXML
  private MFXToggleButton listViewBtn;

  @FXML
  private ScrollPane postsScrollPane;

  @FXML
  private VBox postsContainer;

  @FXML
  private MFXButton loadMoreBtn;

  @FXML
  private VBox categoriesContainer;

  @FXML
  private FlowPane popularTagsContainer;

  @FXML
  private VBox trendingContainer;

  @FXML
  private HBox loadingContainer;

  @FXML
  private HBox authSection;

  @FXML
  private HBox userSection;

  @FXML
  private MFXButton loginBtn;

  @FXML
  private MFXButton signupBtn;

  @FXML
  private MFXButton userMenuBtn;

  @FXML
  private Label userLabel;

  private final PostService postService;
  private final TagService tagService;
  private boolean isGridView = true;
  private int currentPage = 0;
  private static final int PAGE_SIZE = 10;

  public HomeController() {
    this.postService = new PostService();
    this.tagService = new TagService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Home Controller");
    setupUI();
    loadInitialData();
  }

  private void setupUI() {
    setupAuthentication();
    setupSearchAndFilters();
    setupViewToggle();
    setupLoadMore();
    setupCategories();
    setupTags();
    setupTrending();
  }

  private void setupAuthentication() {
    // Check if user is logged in and show appropriate section
    boolean isLoggedIn = AuthContext.getInstance().getCurrentUser() != null;
    
    authSection.setVisible(!isLoggedIn);
    authSection.setManaged(!isLoggedIn);
    
    userSection.setVisible(isLoggedIn);
    userSection.setManaged(isLoggedIn);
    
    if (isLoggedIn) {
      userLabel.setText("Welcome, " + AuthContext.getInstance().getCurrentUser().getUsername());
      setupUserMenu();
    }
    
    // Setup button actions
    loginBtn.setOnAction(e -> navigateToLogin());
    signupBtn.setOnAction(e -> navigateToSignup());
  }

  private void setupSearchAndFilters() {
    categoryComboBox.getItems().addAll("All Categories", "Technology", "Design", "Business", "Lifestyle");
    categoryComboBox.getSelectionModel().selectFirst();

    sortComboBox.getItems().addAll("Latest", "Most Popular", "Most Viewed", "Most Commented");
    sortComboBox.getSelectionModel().selectFirst();

    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
      performSearch(newVal);
    });

    categoryComboBox.setOnAction(e -> performSearch(searchField.getText()));
    sortComboBox.setOnAction(e -> performSearch(searchField.getText()));
  }

  private void setupViewToggle() {
    gridViewBtn.setOnAction(e -> setGridView(true));
    listViewBtn.setOnAction(e -> setGridView(false));

    setGridView(true);
  }

  private void setupLoadMore() {
    loadMoreBtn.setOnAction(e -> loadMorePosts());
  }

  private void setupCategories() {
    loadCategories();
  }

  private void setupTags() {
    loadPopularTags();
  }

  private void setupTrending() {
    loadTrendingPosts();
  }

  private void loadInitialData() {
    showLoading(true);
    loadFeaturedPosts();
    loadPosts();
    hideLoadingAfterDelay();
  }

  private void loadFeaturedPosts() {
    try {
      featuredPostsContainer.getChildren().clear();
      List<Post> allPosts = postService.getPublishedPosts();

      // Take first 5 as featured for demo
      int featuredCount = Math.min(5, allPosts.size());
      for (int i = 0; i < featuredCount; i++) {
        VBox postCard = createFeaturedPostCard(allPosts.get(i));
        featuredPostsContainer.getChildren().add(postCard);
      }
      logger.info("Loaded {} featured posts", featuredCount);
    } catch (Exception e) {
      logger.error("Failed to load featured posts", e);
    }
  }

  private void loadPosts() {
    try {
      if (currentPage == 0) {
        postsContainer.getChildren().clear();
      }

      List<Post> posts = postService.getPublishedPosts();

      // Simple pagination simulation
      int fromIndex = currentPage * PAGE_SIZE;
      int toIndex = Math.min(fromIndex + PAGE_SIZE, posts.size());

      if (fromIndex < posts.size()) {
        List<Post> pagePosts = posts.subList(fromIndex, toIndex);
        postsCountLabel.setText("Latest Posts (" + posts.size() + ")");

        for (Post post : pagePosts) {
          if (isGridView) {
            VBox postCard = createGridPostCard(post);
            postsContainer.getChildren().add(postCard);
          } else {
            VBox postCard = createListPostCard(post);
            postsContainer.getChildren().add(postCard);
          }
        }
      }

      loadMoreBtn.setVisible(toIndex < posts.size());
      logger.info("Loaded {} posts on page {}", Math.min(PAGE_SIZE, posts.size() - fromIndex), currentPage);
    } catch (Exception e) {
      logger.error("Failed to load posts", e);
    }
  }

  private void loadMorePosts() {
    currentPage++;
    loadPosts();
  }

  private void loadCategories() {
    try {
      categoriesContainer.getChildren().clear();

      // Demo categories
      String[] demoCategories = { "Technology", "Design", "Business", "Lifestyle", "Tutorial", "News" };
      for (String category : demoCategories) {
        Label categoryLabel = new Label(category);
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-cursor: hand;");
        categoryLabel.setOnMouseClicked(e -> filterByCategory(category));
        categoriesContainer.getChildren().add(categoryLabel);
      }
    } catch (Exception e) {
      logger.error("Failed to load categories", e);
    }
  }

  private void loadPopularTags() {
    try {
      popularTagsContainer.getChildren().clear();
      List<Tag> tags = tagService.getAllTags();

      // Take first 10 as popular for demo
      int popularCount = Math.min(10, tags.size());
      for (int i = 0; i < popularCount; i++) {
        Tag tag = tags.get(i);
        MFXButton tagButton = new MFXButton("#" + tag.getName());
        tagButton.setStyle(
            "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 12;");
        tagButton.setOnAction(e -> filterByTag(tag));
        popularTagsContainer.getChildren().add(tagButton);
      }
    } catch (Exception e) {
      logger.error("Failed to load popular tags", e);
    }
  }

  private void loadTrendingPosts() {
    try {
      trendingContainer.getChildren().clear();
      List<Post> allPosts = postService.getPublishedPosts();

      // Take first 5 as trending for demo
      int trendingCount = Math.min(5, allPosts.size());
      for (int i = 0; i < trendingCount; i++) {
        HBox trendingItem = createTrendingItem(allPosts.get(i));
        trendingContainer.getChildren().add(trendingItem);
      }
    } catch (Exception e) {
      logger.error("Failed to load trending posts", e);
    }
  }

  private VBox createFeaturedPostCard(Post post) {
    VBox card = new VBox(10);
    card.setStyle(
        "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
    card.setPrefWidth(300);
    card.setCursor(javafx.scene.Cursor.HAND);

    ImageView imageView = new ImageView();
    try {
      // Use placeholder image
      imageView.setImage(new Image("file:src/main/resources/images/featured-post-placeholder.jpg"));
      imageView.setFitWidth(270);
      imageView.setFitHeight(150);
      imageView.setPreserveRatio(true);
      imageView.setStyle("-fx-background-radius: 8;");
    } catch (Exception e) {
      logger.debug("Featured post image not found for post {}", post.getId());
    }

    Label titleLabel = new Label(post.getTitle());
    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
    titleLabel.setWrapText(true);

    Label metaLabel = new Label("By Author • " + formatDate(post.getCreatedAt()));
    metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

    card.getChildren().addAll(imageView, titleLabel, metaLabel);
    card.setOnMouseClicked(e -> openPost(post));

    return card;
  }

  private VBox createGridPostCard(Post post) {
    VBox card = new VBox(15);
    card.setStyle(
        "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
    card.setPrefWidth(300);
    card.setCursor(javafx.scene.Cursor.HAND);

    ImageView imageView = new ImageView();
    try {
      imageView.setImage(new Image("file:src/main/resources/images/post-placeholder.jpg"));
      imageView.setFitWidth(260);
      imageView.setFitHeight(140);
      imageView.setPreserveRatio(true);
      imageView.setStyle("-fx-background-radius: 8;");
    } catch (Exception e) {
      logger.debug("Post image not found for post {}", post.getId());
    }

    Label titleLabel = new Label(post.getTitle());
    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
    titleLabel.setWrapText(true);

    Label excerptLabel = new Label(getExcerpt(post.getContent(), 100));
    excerptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-line-spacing: 1.4;");
    excerptLabel.setWrapText(true);

    HBox metaBox = new HBox(15);
    metaBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    Label authorLabel = new Label("Author");
    authorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
    Label dateLabel = new Label(formatDate(post.getCreatedAt()));
    dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
    metaBox.getChildren().addAll(authorLabel, dateLabel);

    card.getChildren().addAll(imageView, titleLabel, excerptLabel, metaBox);
    card.setOnMouseClicked(e -> openPost(post));

    return card;
  }

  private VBox createListPostCard(Post post) {
    HBox card = new HBox(20);
    card.setStyle(
        "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
    card.setCursor(javafx.scene.Cursor.HAND);

    ImageView imageView = new ImageView();
    try {
      imageView.setImage(new Image("file:src/main/resources/images/post-placeholder.jpg"));
      imageView.setFitWidth(200);
      imageView.setFitHeight(120);
      imageView.setPreserveRatio(true);
      imageView.setStyle("-fx-background-radius: 8;");
    } catch (Exception e) {
      logger.debug("Post image not found for post {}", post.getId());
    }

    VBox contentBox = new VBox(10);
    HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

    Label titleLabel = new Label(post.getTitle());
    titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
    titleLabel.setWrapText(true);

    Label excerptLabel = new Label(getExcerpt(post.getContent(), 150));
    excerptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-line-spacing: 1.4;");
    excerptLabel.setWrapText(true);

    HBox metaBox = new HBox(20);
    metaBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    Label authorLabel = new Label("By Author");
    authorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
    Label dateLabel = new Label(formatDate(post.getCreatedAt()));
    dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
    Label viewsLabel = new Label("👁️ " + post.getViews() + " views");
    viewsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
    metaBox.getChildren().addAll(authorLabel, dateLabel, viewsLabel);

    contentBox.getChildren().addAll(titleLabel, excerptLabel, metaBox);

    card.getChildren().addAll(imageView, contentBox);
    card.setOnMouseClicked(e -> openPost(post));

    VBox wrapper = new VBox(10);
    wrapper.getChildren().add(card);
    return wrapper;
  }

  private HBox createTrendingItem(Post post) {
    HBox item = new HBox(10);
    item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    item.setStyle("-fx-padding: 5; -fx-cursor: hand;");

    Label numberLabel = new Label((trendingContainer.getChildren().size() + 1) + ".");
    numberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #667eea; -fx-min-width: 20;");

    Label titleLabel = new Label(post.getTitle());
    titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
    titleLabel.setWrapText(true);

    item.getChildren().addAll(numberLabel, titleLabel);
    item.setOnMouseClicked(e -> openPost(post));

    return item;
  }

  private void setGridView(boolean grid) {
    isGridView = grid;
    if (grid) {
      gridViewBtn.setStyle(
          "-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 12;");
      listViewBtn
          .setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 5; -fx-padding: 8 12;");
    } else {
      gridViewBtn
          .setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 5; -fx-padding: 8 12;");
      listViewBtn.setStyle(
          "-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 12;");
    }
    currentPage = 0;
    loadPosts();
  }

  private void performSearch(String searchTerm) {
    logger.info("Performing search: {}", searchTerm);
    currentPage = 0;
    loadPosts();
  }

  private void filterByCategory(String category) {
    logger.info("Filtering by category: {}", category);
    categoryComboBox.setValue(category);
    performSearch(searchField.getText());
  }

  private void filterByTag(Tag tag) {
    logger.info("Filtering by tag: {}", tag.getName());
    searchField.setText("#" + tag.getName());
    performSearch("#" + tag.getName());
  }

  private void openPost(Post post) {
    logger.info("Opening post: {}", post.getId());
    Navigator.getInstance().goTo(Routes.POST_VIEW);
  }

  private String getExcerpt(String content, int maxLength) {
    if (content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength) + "...";
  }

  private String formatDate(LocalDateTime date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    return date.format(formatter);
  }

  private void showLoading(boolean show) {
    loadingContainer.setVisible(show);
    loadingContainer.setManaged(show);
  }

  private void hideLoadingAfterDelay() {
    Platform.runLater(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      showLoading(false);
    });
  }

  private void navigateToLogin() {
    logger.info("Navigating to login");
    Navigator.getInstance().goTo(Routes.LOGIN);
  }

  private void navigateToSignup() {
    logger.info("Navigating to signup");
    Navigator.getInstance().goTo(Routes.SIGNUP);
  }

  private void logout() {
    logger.info("Logging out user");
    AuthContext.getInstance().logout();
    Navigator.getInstance().goTo(Routes.LOGIN);
  }

  private void setupUserMenu() {
    ContextMenu userMenu = new ContextMenu();
    
    // Admin menu item
    MenuItem adminItem = new MenuItem("⚙️ Admin");
    adminItem.setOnAction(e -> navigateToAdmin());
    
    // Profile menu item
    MenuItem profileItem = new MenuItem("👤 Profile");
    profileItem.setOnAction(e -> navigateToProfile());
    
    // Logout menu item
    MenuItem logoutItem = new MenuItem("🚪 Logout");
    logoutItem.setOnAction(e -> logout());
    
    userMenu.getItems().addAll(adminItem, profileItem, logoutItem);
    
    userMenuBtn.setOnMouseClicked(e -> {
      userMenu.show(userMenuBtn, e.getScreenX(), e.getScreenY());
    });
  }

  private void navigateToProfile() {
    logger.info("Navigating to profile");
    Navigator.getInstance().goTo(Routes.USER_PROFILE);
  }

  private void navigateToAdmin() {
    logger.info("Navigating to admin dashboard");
    Navigator.getInstance().goTo(Routes.DASHBOARD);
  }
}
