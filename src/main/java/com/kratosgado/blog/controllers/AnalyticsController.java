package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AnalyticsController {
  private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

  @FXML
  private MFXComboBox<String> timeRangeCombo;

  @FXML
  private MFXButton exportReportBtn;

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
  }
}
