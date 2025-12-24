package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

  @FXML
  private void initialize() {
    logger.debug("Initializing Dashboard Home Controller");
    loadDashboardData();
  }

  private void loadDashboardData() {
    try {
      totalPostsLabel.setText("0");
      totalCommentsLabel.setText("0");
      totalViewsLabel.setText("0");
      totalTagsLabel.setText("0");

      logger.info("Dashboard data loaded successfully");
    } catch (Exception e) {
      logger.error("Failed to load dashboard data", e);
    }
  }
}
