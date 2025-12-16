package com.kratosgado.blog;

import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
// import io.github.palexdev.materialfx.theming.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
    Scene scene = new Scene(root, 600, 500);
    primaryStage.setTitle("Blog");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
