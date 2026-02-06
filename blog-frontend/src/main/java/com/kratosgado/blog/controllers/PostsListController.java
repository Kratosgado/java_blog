package com.kratosgado.blog.controllers;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.UiUtils;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.widgets.CustomButton;
import com.kratosgado.blog.utils.widgets.CustomButton.ButtonType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PostsListController {
  private static final Logger logger = LoggerFactory.getLogger(PostsListController.class);

  @FXML
  private TextField searchField;

  @FXML
  private ComboBox<String> sortComboBox;

  @FXML
  private ComboBox<String> filterComboBox;

  @FXML
  private Button createNewPostBtn;

  @FXML
  private Button searchBtn;

  @FXML
  private Button clearBtn;

  @FXML
  private TableView<Post> postsTable;

  @FXML
  private Label pageLabel;

  @FXML
  private Button prevPageBtn;

  @FXML
  private Button nextPageBtn;

  @FXML
  private Label totalPostsLabel;

  private final PostService postService;
  private int currentPage = 1;
  private int pageSize = 10;
  private String currentFilter = "All";
  private String currentSort = "Latest";

  @Inject
  public PostsListController(PostService postService) {
    this.postService = postService;
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Posts List Controller");
    setupUI();
    loadPosts();
  }

  private void setupUI() {
    searchBtn.setOnAction(e -> searchPosts());
    clearBtn.setOnAction(e -> clearSearch());
    createNewPostBtn.setOnAction(e -> createNewPost());
    prevPageBtn.setOnAction(e -> previousPage());
    nextPageBtn.setOnAction(e -> nextPage());

    sortComboBox.getItems().addAll("Latest", "Oldest", "Most Viewed", "Title A-Z");
    sortComboBox.setValue("Latest");
    sortComboBox.setOnAction(e -> handleSortChange());

    filterComboBox.getItems().addAll("All", "Published", "Draft");
    filterComboBox.setValue("All");
    filterComboBox.setOnAction(e -> handleFilterChange());

    setupTableColumns();
  }

  private void setupTableColumns() {
    postsTable.getColumns().clear();

    // Title Column with custom cell renderer
    TableColumn<Post, String> titleCol = new TableColumn<>("Title");
    titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
    titleCol.setPrefWidth(280);
    titleCol.setCellFactory(column -> new TableCell<Post, String>() {
      private final Text text = new Text();

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          setText(null);
        } else {
          text.setText(item);
          text.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; -fx-fill: #111827;");
          text.setWrappingWidth(260);
          setGraphic(text);

          // Tooltip for long titles
          if (item.length() > 40) {
            Tooltip tooltip = new Tooltip(item);
            tooltip.setShowDelay(Duration.millis(300));
            setTooltip(tooltip);
          }
        }
      }
    });

    // Author Column
    TableColumn<Post, String> authorCol = new TableColumn<>("Author");
    authorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));
    authorCol.setPrefWidth(140);
    authorCol.setCellFactory(column -> new TableCell<Post, String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        }
      }
    });

    // Date Column with formatted date
    TableColumn<Post, LocalDateTime> dateCol = new TableColumn<>("Published");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    dateCol.setPrefWidth(130);
    dateCol.setCellFactory(column -> new TableCell<Post, LocalDateTime>() {
      @Override
      protected void updateItem(LocalDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setStyle("");
        } else {
          setText(UiUtils.formatDate(item));
          setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px; -fx-alignment: CENTER;");
          setAlignment(Pos.CENTER);
        }
      }
    });

    // Views Column with icon and formatted number
    TableColumn<Post, Integer> viewsCol = new TableColumn<>("Views");
    viewsCol.setCellValueFactory(new PropertyValueFactory<>("views"));
    viewsCol.setPrefWidth(90);
    viewsCol.setCellFactory(column -> new TableCell<Post, Integer>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setStyle("");
        } else {
          setText(String.format("%,d", item));
          setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px; -fx-font-weight: 600;");
          setAlignment(Pos.CENTER);
        }
      }
    });

    // Status Column with badge styling
    TableColumn<Post, PostStatus> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setPrefWidth(110);
    statusCol.setCellFactory(column -> new TableCell<Post, PostStatus>() {
      @Override
      protected void updateItem(PostStatus item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setGraphic(null);
          setText(null);
        } else {
          Label badge = new Label(item.name().toUpperCase());
          badge.setStyle(getStatusStyle(item));
          badge.setAlignment(Pos.CENTER);
          badge.setMaxWidth(Double.MAX_VALUE);
          setGraphic(badge);
          setAlignment(Pos.CENTER);
        }
      }

      private String getStatusStyle(PostStatus status) {
        if (status == PostStatus.published) {
          return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; " +
              "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 700;";
        } else if (status == PostStatus.draft) {
          return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
              "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 700;";
        } else {
          return "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; " +
              "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 700;";
        }
      }
    });

    // Actions Column with buttons
    TableColumn<Post, Void> actionsCol = new TableColumn<>("Actions");
    actionsCol.setPrefWidth(200);
    actionsCol.setSortable(false);
    actionsCol.setCellFactory(column -> new TableCell<Post, Void>() {

      private final Button viewBtn = new CustomButton("View", event -> {
        Post post = getTableView().getItems().get(getIndex());
        viewPost(post);
      });
      private final Button editBtn = new CustomButton("Edit", ButtonType.INFO, event -> {
        Post post = getTableView().getItems().get(getIndex());
        editPost(post);
      });
      private final Button deleteBtn = new CustomButton("Delete", ButtonType.ERROR, event -> {
        Post post = getTableView().getItems().get(getIndex());
        deletePost(post);
      });
      private final HBox actionBox = new HBox(8);

      {
        actionBox.setAlignment(Pos.CENTER);
        actionBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);

      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          setGraphic(actionBox);
        }
      }
    });

    postsTable.getColumns().addAll(titleCol, authorCol, dateCol, viewsCol, statusCol, actionsCol);

    // Set row factory for alternating row colors
    postsTable.setRowFactory(tv -> new javafx.scene.control.TableRow<Post>() {
      @Override
      protected void updateItem(Post item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setStyle("");
        } else {
          if (getIndex() % 2 == 0) {
            setStyle("-fx-background-color: #ffffff;");
          } else {
            setStyle("-fx-background-color: #f9fafb;");
          }

          // Add hover effect
          setOnMouseEntered(e -> setStyle("-fx-background-color: #f3f4f6; -fx-cursor: hand;"));
          setOnMouseExited(e -> {
            if (getIndex() % 2 == 0) {
              setStyle("-fx-background-color: #ffffff;");
            } else {
              setStyle("-fx-background-color: #f9fafb;");
            }
          });
        }
      }
    });
  }

  private void loadPosts() {
    try {
      // Get all posts (page 0, size 1000 to get all)
      var pageResponse = postService.getAllPosts(0, 1000);
      // Convert PageResponse<PostResponse> to List<Post>
      var allPosts = pageResponse.content().stream().map(pr -> {
        Post p = new Post();
        p.setId(pr.getId());
        p.setTitle(pr.getTitle());
        // Get content if PostDetails
        if (pr instanceof com.kratosgado.blog.dtos.response.PostResponse.PostDetails) {
          p.setContent(((com.kratosgado.blog.dtos.response.PostResponse.PostDetails) pr).getContent());
          p.setUpdatedAt(((com.kratosgado.blog.dtos.response.PostResponse.PostDetails) pr).getUpdatedAt());
        }
        p.setExcerpt(pr.getExcerpt());
        p.setStatus(pr.getStatus());
        p.setCoverImage(pr.getCoverImage());
        // Get user ID from user object if available
        if (pr instanceof com.kratosgado.blog.dtos.response.PostResponse.WithUser) {
          var user = ((com.kratosgado.blog.dtos.response.PostResponse.WithUser) pr).getUser();
          if (user != null) {
            p.setUserId(user.getId());
          }
        }
        // Get category from category object if available
        if (pr instanceof com.kratosgado.blog.dtos.response.PostResponse.WithCategory) {
          var category = ((com.kratosgado.blog.dtos.response.PostResponse.WithCategory) pr).getCategory();
          if (category != null) {
            p.setCategory(com.kratosgado.blog.models.Category.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build());
          }
        }
        p.setCreatedAt(pr.getCreatedAt());
        return p;
      }).toList();
      
      var currentUserPosts = allPosts.stream()
          .filter(p -> p.getUserId().equals(AuthContext.getInstance().getCurrentUser().getId()))
          .toList();

      var filteredPosts = filterPosts(currentUserPosts);
      var sortedPosts = sortPosts(filteredPosts);

      // Update total posts label
      totalPostsLabel.setText(sortedPosts.size() + " post" + (sortedPosts.size() != 1 ? "s" : ""));

      int totalPages = (int) Math.ceil((double) sortedPosts.size() / pageSize);
      pageLabel.setText("Page " + currentPage + " of " + Math.max(1, totalPages));

      int startIndex = (currentPage - 1) * pageSize;
      int endIndex = Math.min(startIndex + pageSize, sortedPosts.size());

      ObservableList<Post> pageData = FXCollections.observableArrayList(
          sortedPosts.subList(startIndex, endIndex));
      postsTable.setItems(pageData);

      // Disable pagination buttons appropriately
      prevPageBtn.setDisable(currentPage == 1);
      nextPageBtn.setDisable(currentPage >= totalPages || sortedPosts.isEmpty());

      logger.info("Posts loaded successfully: {} posts", sortedPosts.size());
    } catch (Exception e) {
      logger.error("Failed to load posts", e);
    }
  }

  private java.util.List<Post> filterPosts(java.util.List<Post> posts) {
    return switch (currentFilter) {
      case "Published" -> posts.stream().filter(p -> p.getStatus() == PostStatus.published).toList();
      case "Draft" -> posts.stream().filter(p -> p.getStatus() == PostStatus.draft).toList();
      default -> posts;
    };
  }

  private java.util.List<Post> sortPosts(java.util.List<Post> posts) {
    return switch (currentSort) {
      case "Oldest" -> posts.stream()
          .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())).toList();
      case "Most Viewed" -> posts.stream()
          .sorted((a, b) -> b.getViews() - a.getViews()).toList();
      case "Title A-Z" -> posts.stream()
          .sorted((a, b) -> a.getTitle().compareTo(b.getTitle())).toList();
      default -> posts.stream()
          .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).toList();
    };
  }

  private void handleSortChange() {
    if (sortComboBox.getValue() != null) {
      currentSort = sortComboBox.getValue();
      currentPage = 1;
      loadPosts();
    }
  }

  private void handleFilterChange() {
    if (filterComboBox.getValue() != null) {
      currentFilter = filterComboBox.getValue();
      currentPage = 1;
      loadPosts();
    }
  }

  private void searchPosts() {
    String query = searchField.getText().toLowerCase();
    if (!query.isEmpty()) {
      try {
        // Get all posts (page 0, size 1000 to get all)
        var pageResponse = postService.getAllPosts(0, 1000);
        // Convert PageResponse<PostResponse> to List<Post>
        var allPosts = pageResponse.content().stream().map(pr -> {
          Post p = new Post();
          p.setId(pr.getId());
          p.setTitle(pr.getTitle());
          // Get content if PostDetails
          if (pr instanceof com.kratosgado.blog.dtos.response.PostResponse.PostDetails) {
            p.setContent(((com.kratosgado.blog.dtos.response.PostResponse.PostDetails) pr).getContent());
            p.setUpdatedAt(((com.kratosgado.blog.dtos.response.PostResponse.PostDetails) pr).getUpdatedAt());
          }
          p.setExcerpt(pr.getExcerpt());
          p.setStatus(pr.getStatus());
          p.setCoverImage(pr.getCoverImage());
          // Get user ID from user object if available
          if (pr instanceof com.kratosgado.blog.dtos.response.PostResponse.WithUser) {
            var user = ((com.kratosgado.blog.dtos.response.PostResponse.WithUser) pr).getUser();
            if (user != null) {
              p.setUserId(user.getId());
            }
          }
          // Get category from category object if available
          if (pr instanceof com.kratosgado.blog.dtos.response.PostResponse.WithCategory) {
            var category = ((com.kratosgado.blog.dtos.response.PostResponse.WithCategory) pr).getCategory();
            if (category != null) {
              p.setCategory(com.kratosgado.blog.models.Category.builder()
                  .id(category.getId())
                  .name(category.getName())
                  .slug(category.getSlug())
                  .build());
            }
          }
          p.setCreatedAt(pr.getCreatedAt());
          return p;
        }).toList();
        
        var filtered = allPosts.stream()
            .filter(p -> p.getTitle().toLowerCase().contains(query) || p.getContent().toLowerCase().contains(query))
            .toList();

        ObservableList<Post> results = FXCollections.observableArrayList(filtered);
        postsTable.setItems(results);
        pageLabel.setText("Search Results: " + filtered.size() + " posts");
        logger.info("Search executed with query: {}", query);
      } catch (Exception e) {
        logger.error("Failed to search posts", e);
      }
    }
  }

  private void clearSearch() {
    searchField.clear();
    currentPage = 1;
    loadPosts();
  }

  @FXML
  private void createNewPost() {
    logger.info("Creating new post");
    try {
      // Navigate to create post page
      DashboardController.instance().goToCreatePost();
      logger.debug("Navigated to create post screen");
    } catch (Exception e) {
      logger.error("Failed to navigate to create post screen", e);
    }
  }

  private void previousPage() {
    if (currentPage > 1) {
      currentPage--;
      loadPosts();
      logger.info("Previous page clicked");
    }
  }

  private void nextPage() {
    currentPage++;
    loadPosts();
    logger.info("Next page clicked");
  }

  private void viewPost(Post post) {
    logger.info("Viewing post: {}", post.getTitle());
    try {
      com.kratosgado.blog.utils.Navigator.getInstance().goTo(Routes.POST_VIEW, post.getId());
    } catch (Exception e) {
      logger.error("Failed to navigate to post view", e);
    }
  }

  private void editPost(Post post) {
    logger.info("Editing post: {}", post.getTitle());
    try {
      com.kratosgado.blog.utils.Navigator.getInstance().goTo(Routes.EDIT_POST, post.getId());
    } catch (Exception e) {
      logger.error("Failed to navigate to edit post screen", e);
    }
  }

  private void deletePost(Post post) {
    logger.info("Deleting post: {}", post.getTitle());
    try {
      if (com.kratosgado.blog.utils.DialogUtils.showConfirmation(
          "Delete Post",
          "Are you sure you want to delete this post? This action cannot be undone.")) {
        postService.deletePost(post.getId());
        loadPosts();
      }
    } catch (Exception e) {
      logger.error("Failed to delete post", e);
    }
  }
}
