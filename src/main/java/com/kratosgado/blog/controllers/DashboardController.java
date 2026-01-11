
package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.notifications.ToastNotification;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {
  private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
  private static DashboardController instance;

  @FXML
  private Label titleLabel;

  @FXML
  private Button logoutBtn;

  @FXML
  private StackPane contentArea;

  @FXML
  private TextField searchField;

  @FXML
  private Label userLabel;

  @FXML
  private Button dashboardBtn;

  @FXML
  private Button postsBtn;

  @FXML
  private Button createPostBtn;

  @FXML
  private Button commentsBtn;

  @FXML
  private Button tagsBtn;

  @FXML
  private Button categoriesBtn;

  @FXML
  private Button analyticsBtn;

  @FXML
  private Button profileBtn;

  @FXML
  private Button settingsBtn;

  public DashboardController() {
    instance = this;
  }

  public static DashboardController instance() {
    return instance;
  }

  public void goToPosts() {
    postsBtn.fire();
  }

  public void goToCreatePost() {
    createPostBtn.fire();
  }

  @FXML
  private void initialize() {
    String username = AuthContext.getInstance().getCurrentUser().getUsername();
    userLabel.setText("Welcome, " + username);

    titleLabel.setOnMouseClicked(e -> Navigator.getInstance().goTo(Routes.HOME));
    titleLabel.setCursor(javafx.scene.Cursor.HAND);

    logoutBtn.setOnAction(e -> logout());
    dashboardBtn.setOnAction(e -> loadContent(Routes.DASHBOARD_HOME, dashboardBtn));
    postsBtn.setOnAction(e -> loadContent(Routes.POSTS, postsBtn));
    createPostBtn.setOnAction(e -> loadContent(Routes.CREATE_POST, createPostBtn));
    commentsBtn.setOnAction(e -> loadContent(Routes.COMMENTS, commentsBtn));
    tagsBtn.setOnAction(e -> loadContent(Routes.TAGS, tagsBtn));
    categoriesBtn.setOnAction(e -> loadContent(Routes.CATEGORIES, categoriesBtn));
    analyticsBtn.setOnAction(e -> loadContent(Routes.ANALYTICS, analyticsBtn));
    profileBtn.setOnAction(e -> loadContent(Routes.PROFILE, profileBtn));
    settingsBtn.setOnAction(e -> loadContent(Routes.SETTINGS, settingsBtn));

    loadContent(Routes.DASHBOARD_HOME, dashboardBtn);
  }

  public void loadContent(String route, Button button) {
    try {
      contentArea.getChildren().clear();
      Parent content = Navigator.getInstance().getSubScene(route);
      if (content != null) {
        contentArea.getChildren().add(content);
        updateActiveButton(button);
        logger.debug("Loaded content: {}", route);
      } else {
        ToastNotification.error("Failed to load " + route);
      }
    } catch (Exception e) {
      logger.error("Failed to load content: {}", route, e);
      ToastNotification.error("Failed to load " + route);
    }
  }

  private void updateActiveButton(Button activeButton) {
    for (Button btn : new Button[] { dashboardBtn, postsBtn, createPostBtn, commentsBtn, tagsBtn, categoriesBtn,
        analyticsBtn, profileBtn, settingsBtn }) {
      btn.setStyle(
          "-fx-background-color: transparent; -fx-text-fill: #333; -fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 14px;");
    }
    activeButton.setStyle(
        "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 14px;");
  }

  private void logout() {
    ToastNotification.info("Logging out...");
    AuthContext.getInstance().logout();
    Navigator.getInstance().goTo(Routes.LOGIN);
  }

}
