package com.kratosgado.blog.controllers;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.utils.context.AuthContext;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsController {
  private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

  @FXML
  private ComboBox<String> timeRangeCombo;

  @FXML
  private Button exportReportBtn;

  @FXML
  private Label totalViewsLabel;

  @FXML
  private Label viewsChangeLabel;

  @FXML
  private Label engagementLabel;

  @FXML
  private Label engagementChangeLabel;

  @FXML
  private Label avgReadTimeLabel;

  @FXML
  private Label readTimeChangeLabel;

  @FXML
  private LineChart<?, ?> viewsChart;

  @FXML
  private TableView<Post> topPostsTable;

  @FXML
  private TableColumn<Post, String> topPostTitleColumn;

  @FXML
  private TableColumn<Post, Integer> topPostViewsColumn;

  @FXML
  private TableColumn<Post, Integer> topPostCommentsColumn;

  @FXML
  private PieChart tagPerformanceChart;

  @FXML
  private Label searchQueryLabel;

  @FXML
  private Label searchBeforeLabel;

  @FXML
  private Label searchAfterLabel;

  @FXML
  private Label searchImprovementLabel;

  @FXML
  private Label listQueryLabel;

  @FXML
  private Label listBeforeLabel;

  @FXML
  private Label listAfterLabel;

  @FXML
  private Label listImprovementLabel;

  @FXML
  private Button runBenchmarkBtn;

  @FXML
  private Label benchmarkStatusLabel;

  private final PostService postService;
  private final CommentService commentService;
  private final TagService tagService;

  @Inject
  public AnalyticsController(PostService postService, CommentService commentService, TagService tagService) {
    this.postService = postService;
    this.commentService = commentService;
    this.tagService = tagService;
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Analytics Controller");
    setupUI();
    setupTableColumns();
    loadAnalytics();
  }

  private void setupUI() {
    timeRangeCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months", "Last Year");
    timeRangeCombo.setValue("Last 30 Days");
    timeRangeCombo.setOnAction(e -> loadAnalytics());

    exportReportBtn.setOnAction(e -> exportReport());
    runBenchmarkBtn.setOnAction(e -> runPerformanceBenchmark());
  }

  private void setupTableColumns() {
    topPostTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
    topPostViewsColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getViews()).asObject());
    topPostCommentsColumn.setCellValueFactory(data -> {
      int commentCount = commentService.getCommentCountForPost(data.getValue().getId());
      return new SimpleIntegerProperty(commentCount).asObject();
    });
  }

  private void loadAnalytics() {
    try {
      updateMetrics();
      loadTopPosts();
      loadTagPerformance();
      logger.info("Analytics loaded successfully");
    } catch (Exception e) {
      logger.error("Failed to load analytics", e);
    }
  }

  private void updateMetrics() {
    try {
      int currentUserId = AuthContext.getInstance().getCurrentUser().getId();
      var posts = postService.getPostsByUserId(currentUserId);

      long totalViews = postService.getTotalViews(currentUserId);
      totalViewsLabel.setText(String.valueOf(totalViews));
      viewsChangeLabel.setText("+0% from last period");

      int totalComments = 0;
      for (var post : posts) {
        totalComments += commentService.getCommentCountForPost(post.getId());
      }

      double engagement = posts.isEmpty() ? 0 : (double) totalComments / posts.size();
      engagementLabel.setText(String.format("%.1f%%", engagement));
      engagementChangeLabel.setText("+0% from last period");

      // Calculate average read time (assume 200 words per minute)
      int totalWords = posts.stream()
          .mapToInt(p -> p.getContent() != null ? p.getContent().split("\\s+").length : 0)
          .sum();
      double avgReadTime = posts.isEmpty() ? 0 : (double) totalWords / posts.size() / 200;
      avgReadTimeLabel.setText(String.format("%.1f min", avgReadTime));
      readTimeChangeLabel.setText("+0% from last period");

    } catch (Exception e) {
      logger.error("Failed to update metrics", e);
    }
  }

  private void loadTopPosts() {
    try {
      int currentUserId = AuthContext.getInstance().getCurrentUser().getId();
      var posts = postService.getPostsByUserId(currentUserId);

      // Sort by views and take top 10
      posts.sort((p1, p2) -> Integer.compare(p2.getViews(), p1.getViews()));
      var topPosts = posts.stream().limit(10).toList();

      topPostsTable.getItems().clear();
      topPostsTable.getItems().addAll(topPosts);

    } catch (Exception e) {
      logger.error("Failed to load top posts", e);
    }
  }

  private void loadTagPerformance() {
    try {
      int currentUserId = AuthContext.getInstance().getCurrentUser().getId();
      var posts = postService.getPostsByUserId(currentUserId);

      // Count posts per tag
      Map<String, Integer> tagCounts = new HashMap<>();
      for (var post : posts) {
        List<Tag> tags = tagService.getTagsByPostId(post.getId());
        for (Tag tag : tags) {
          tagCounts.merge(tag.getName(), 1, Integer::sum);
        }
      }

      tagPerformanceChart.getData().clear();
      tagCounts.forEach((tagName, count) -> {
        PieChart.Data slice = new PieChart.Data(tagName, count);
        tagPerformanceChart.getData().add(slice);
      });

    } catch (Exception e) {
      logger.error("Failed to load tag performance", e);
    }
  }

  private void runPerformanceBenchmark() {
    try {
      benchmarkStatusLabel.setText("Running benchmark...");
      benchmarkStatusLabel.setStyle("-fx-text-fill: #6b7280;");

      int currentUserId = AuthContext.getInstance().getCurrentUser().getId();

      // Benchmark post listing  
      long listStart = System.currentTimeMillis();
      var posts = postService.getPostsByUserId(currentUserId);
      long listTime = System.currentTimeMillis() - listStart;

      // Benchmark post access (get first post if available)
      long searchTime = 0;
      if (!posts.isEmpty()) {
        long searchStart = System.currentTimeMillis();
        postService.getPostById(posts.get(0).getId());
        searchTime = System.currentTimeMillis() - searchStart;
      }

      // Display results (showing "after" optimization time)
      searchBeforeLabel.setText("N/A");
      searchAfterLabel.setText(searchTime + " ms");
      searchImprovementLabel.setText("Baseline");

      listBeforeLabel.setText("N/A");
      listAfterLabel.setText(listTime + " ms");
      listImprovementLabel.setText("Baseline");

      benchmarkStatusLabel.setText("Benchmark completed!");
      benchmarkStatusLabel.setStyle("-fx-text-fill: #6b7280;");
      logger.info("Performance benchmark completed: Search={}ms, List={}ms", searchTime, listTime);

    } catch (Exception e) {
      logger.error("Failed to run performance benchmark", e);
      benchmarkStatusLabel.setText("Benchmark failed");
      benchmarkStatusLabel.setStyle("-fx-text-fill: #ef4444;");
    }
  }

  private void exportReport() {
    logger.info("Exporting analytics report");
    try {
      int currentUserId = AuthContext.getInstance().getCurrentUser().getId();
      var posts = postService.getPostsByUserId(currentUserId);

      // Build CSV report
      StringBuilder report = new StringBuilder();
      report.append("Analytics Report\n");
      report.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n\n");
      report.append("Summary:\n");
      report.append("Total Posts: ").append(posts.size()).append("\n");
      report.append("Total Views: ").append(postService.getTotalViews(currentUserId)).append("\n");

      int totalComments = 0;
      for (var post : posts) {
        totalComments += commentService.getCommentCountForPost(post.getId());
      }
      report.append("Total Comments: ").append(totalComments).append("\n\n");

      report.append("Posts Detail:\n");
      report.append("Title,Status,Views,Comments,Created\n");
      for (var post : posts) {
        int commentCount = commentService.getCommentCountForPost(post.getId());
        report.append(String.format("%s,%s,%d,%d,%s\n",
            post.getTitle().replace(",", ";"),
            post.getStatus(),
            post.getViews(),
            commentCount,
            post.getCreatedAt()));
      }

      // Save to file
      javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
      fileChooser.setTitle("Export Analytics Report");
      fileChooser.setInitialFileName("analytics_report_" + java.time.LocalDate.now() + ".csv");
      fileChooser.getExtensionFilters().add(
          new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

      java.io.File file = fileChooser.showSaveDialog(exportReportBtn.getScene().getWindow());
      if (file != null) {
        java.nio.file.Files.writeString(file.toPath(), report.toString());
        logger.info("Analytics report exported successfully to: {}", file.getAbsolutePath());
        com.kratosgado.blog.utils.notifications.ToastNotification.success("Report exported successfully!");
      }
    } catch (Exception e) {
      logger.error("Failed to export analytics report", e);
      com.kratosgado.blog.utils.notifications.ToastNotification.error("Failed to export report");
    }
  }
}
