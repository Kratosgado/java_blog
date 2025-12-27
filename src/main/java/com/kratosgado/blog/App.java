package com.kratosgado.blog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXNotificationCenter;
import io.github.palexdev.materialfx.controls.cell.MFXNotificationCell;
import io.github.palexdev.materialfx.notifications.MFXNotificationCenterSystem;
import io.github.palexdev.materialfx.notifications.MFXNotificationSystem;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  @Override
  public void start(Stage primaryStage) throws Exception {

    UserAgentBuilder.builder().themes(JavaFXThemes.MODENA)
        .themes(MaterialFXStylesheets.forAssemble(true))
        .setDeploy(true)
        .setResolveAssets(true).build().setGlobal();
    MFXNotificationSystem.instance().initOwner(primaryStage);
    MFXNotificationCenterSystem.instance().initOwner(primaryStage);

    MFXNotificationCenter center = MFXNotificationCenterSystem.instance().getCenter();
    center.setCellFactory(notification -> new MFXNotificationCell(center, notification) {
      {
        setPrefHeight(400);
      }
    });
    Navigator navigator = Navigator.getInstance();
    navigator.setStage(primaryStage);
    if (AuthContext.getInstance().getCurrentUser() == null) {
      navigator.goTo(Routes.LOGIN);
    } else {
      navigator.goTo(Routes.HOME);
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
