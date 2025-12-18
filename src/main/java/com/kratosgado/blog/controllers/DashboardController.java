
package com.kratosgado.blog.controllers;

import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class DashboardController {

  @FXML
  private StackPane contentArea;

  @FXML
  private void initialize() {
    contentArea.getChildren().add(Navigator.getInstance().getSubScene(Routes.DASHBOARD_HOME));
  }
}
