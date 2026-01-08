package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.utils.context.AuthContext;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
  private Label totalCommentsLabel;

  @FXML
  private Label commentsChangeLabel;

  @FXML
  private Label totalPostsLabel;

  @FXML
  private Label postsChangeLabel;

  @FXML
  private Label avgEngagementLabel;

  @FXML
  private Label engagementChangeLabel;

  @FXML
  private LineChart<?, ?> viewsChart;

  @FXML
  private BarChart<?, ?> topPostsChart;

  @FXML
  private VBox analyticsContainer;

  private final PostService postService;
  private final CommentService commentService;

  public AnalyticsController() {
    this.postService = new PostService();
    this.commentService = new CommentService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Analytics Controller");
    setupUI();
    loadAnalytics();
  }

  private void setupUI() {
    timeRangeCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months", "Last Year");
    timeRangeCombo.setValue("Last 30 Days");
    timeRangeCombo.setOnAction(e -> loadAnalytics());

    exportReportBtn.setOnAction(e -> exportReport());
  }

  private void loadAnalytics() {
    try {
      updateMetrics();
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
      totalCommentsLabel.setText(String.valueOf(totalComments));
      commentsChangeLabel.setText("+0% from last period");

      totalPostsLabel.setText(String.valueOf(posts.size()));
      postsChangeLabel.setText("+0% from last period");

      double engagement = posts.isEmpty() ? 0 : (double) totalComments / posts.size();
      avgEngagementLabel.setText(String.format("%.1f%%", engagement));
      engagementChangeLabel.setText("+0% from last period");

    } catch (Exception e) {
      logger.error("Failed to update metrics", e);
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
