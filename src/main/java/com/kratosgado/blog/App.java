package com.kratosgado.blog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  @Override
  public void start(Stage primaryStage) throws Exception {
    // Apply modern theme globally
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);

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
