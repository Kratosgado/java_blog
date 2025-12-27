package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PostsListController {
  private static final Logger logger = LoggerFactory.getLogger(PostsListController.class);

  @FXML
  private MFXTextField searchField;

  @FXML
  private MFXComboBox<String> sortComboBox;

  @FXML
  private MFXComboBox<String> filterComboBox;

  @FXML
  private MFXButton createNewPostBtn;

  @FXML
  private MFXButton searchBtn;

  @FXML
  private MFXButton clearBtn;

  @FXML
  private TableView<Post> postsTable;

  @FXML
  private Label pageLabel;

  @FXML
  private MFXButton prevPageBtn;

  @FXML
  private MFXButton nextPageBtn;

  private final PostService postService;
  private int currentPage = 1;
  private int pageSize = 10;
  private String currentFilter = "All";
  private String currentSort = "Latest";

  public PostsListController() {
    this.postService = new PostService();
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

    TableColumn<Post, String> titleCol = new TableColumn<>("Title");
    titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
    titleCol.setPrefWidth(250);

    TableColumn<Post, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setPrefWidth(100);

    TableColumn<Post, String> createdCol = new TableColumn<>("Created");
    createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    createdCol.setPrefWidth(150);

    TableColumn<Post, Integer> viewsCol = new TableColumn<>("Views");
    viewsCol.setCellValueFactory(new PropertyValueFactory<>("views"));
    viewsCol.setPrefWidth(80);

    postsTable.getColumns().addAll(titleCol, statusCol, createdCol, viewsCol);
  }

  private void loadPosts() {
    try {
      var allPosts = postService.getAllPosts();
      var currentUserPosts = allPosts.stream()
          .filter(p -> p.getUserId() == AuthContext.getInstance().getCurrentUser().getId())
          .toList();

      var filteredPosts = filterPosts(currentUserPosts);
      var sortedPosts = sortPosts(filteredPosts);

      int totalPages = (int) Math.ceil((double) sortedPosts.size() / pageSize);
      pageLabel.setText("Page " + currentPage + " of " + Math.max(1, totalPages));

      int startIndex = (currentPage - 1) * pageSize;
      int endIndex = Math.min(startIndex + pageSize, sortedPosts.size());

      ObservableList<Post> pageData = FXCollections.observableArrayList(
          sortedPosts.subList(startIndex, endIndex)
      );
      postsTable.setItems(pageData);

      logger.info("Posts loaded successfully");
    } catch (Exception e) {
      logger.error("Failed to load posts", e);
    }
  }

  private java.util.List<Post> filterPosts(java.util.List<Post> posts) {
    return switch (currentFilter) {
      case "Published" -> posts.stream().filter(p -> "published".equals(p.getStatus())).toList();
      case "Draft" -> posts.stream().filter(p -> "draft".equals(p.getStatus())).toList();
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
        var allPosts = postService.getAllPosts();
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

  private void createNewPost() {
    logger.info("Creating new post");
    try {
      // Navigate to create post page - implement based on your Navigator
    } catch (Exception e) {
      logger.error("Failed to create new post", e);
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
}
