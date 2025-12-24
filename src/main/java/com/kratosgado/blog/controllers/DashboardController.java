
package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {
  private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

  @FXML
  private MFXButton logoutBtn;

  @FXML
  private StackPane contentArea;

  @FXML
  private MFXTextField searchField;

  @FXML
  private Label userLabel;

  @FXML
  private MFXButton dashboardBtn;

  @FXML
  private MFXButton postsBtn;

  @FXML
  private MFXButton createPostBtn;

  @FXML
  private MFXButton commentsBtn;

  @FXML
  private MFXButton tagsBtn;

  @FXML
  private MFXButton analyticsBtn;

  @FXML
  private MFXButton profileBtn;

  @FXML
  private MFXButton settingsBtn;

  @FXML
  private void initialize() {
    String username = AuthContext.getInstance().getCurrentUser().getUsername();
    userLabel.setText("Welcome, " + username);

    logoutBtn.setOnAction(e -> logout());
    dashboardBtn.setOnAction(e -> loadContent(Routes.DASHBOARD_HOME, dashboardBtn));
    postsBtn.setOnAction(e -> loadContent(Routes.POSTS, postsBtn));
    createPostBtn.setOnAction(e -> loadContent(Routes.CREATE_POST, createPostBtn));
    commentsBtn.setOnAction(e -> loadContent(Routes.COMMENTS, commentsBtn));
    tagsBtn.setOnAction(e -> loadContent(Routes.TAGS, tagsBtn));
    analyticsBtn.setOnAction(e -> loadContent(Routes.ANALYTICS, analyticsBtn));
    profileBtn.setOnAction(e -> loadContent(Routes.PROFILE, profileBtn));
    settingsBtn.setOnAction(e -> loadContent(Routes.SETTINGS, settingsBtn));

    loadContent(Routes.DASHBOARD_HOME, dashboardBtn);
  }

  private void loadContent(String route, MFXButton button) {
    try {
      contentArea.getChildren().clear();
      var content = Navigator.getInstance().getSubScene(route);
      if (content != null) {
        contentArea.getChildren().add(content);
        updateActiveButton(button);
        logger.debug("Loaded content: {}", route);
      }
    } catch (Exception e) {
      logger.error("Failed to load content: {}", route, e);
    }
  }

  private void updateActiveButton(MFXButton activeButton) {
    for (MFXButton btn : new MFXButton[] { dashboardBtn, postsBtn, createPostBtn, commentsBtn, tagsBtn, analyticsBtn,
        profileBtn, settingsBtn }) {
      btn.setStyle(
          "-fx-background-color: transparent; -fx-text-fill: #333; -fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 14px;");
    }
    activeButton.setStyle(
        "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 14px;");
  }

  private void logout() {
    AuthContext.getInstance().logout();
    Navigator.getInstance().goTo(Routes.LOGIN);
  }

}
