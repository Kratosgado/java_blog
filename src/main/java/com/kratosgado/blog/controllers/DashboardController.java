
package com.kratosgado.blog.controllers;

import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class DashboardController {

  @FXML
  private MFXButton logoutBtn;

  @FXML
  private StackPane contentArea;

  @FXML
  private void initialize() {
    logoutBtn.setOnAction(e -> logout());
    contentArea.getChildren().add(Navigator.getInstance().getSubScene(Routes.DASHBOARD_HOME));
  }

  private void logout() {
    AuthContext.getInstance().logout();
    Navigator.getInstance().goTo(Routes.LOGIN);
  }

}
