package com.kratosgado.blog;

import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
// import io.github.palexdev.materialfx.theming.Themes;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class App extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {

    UserAgentBuilder.builder().themes(JavaFXThemes.MODENA)
        .themes(MaterialFXStylesheets.forAssemble(true))
        .setDeploy(true)
        .setResolveAssets(true).build().setGlobal();

    Navigator navigator = Navigator.getInstance();
    navigator.setStage(primaryStage);
    if (AuthContext.getInstance().getCurrentUser() == null) {
      navigator.goTo(Routes.LOGIN);
    } else {
      navigator.goTo(Routes.USER_PROFILE);
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
