package com.kratosgado.blog.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import com.kratosgado.blog.App;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

  private Stage stage;
  private final Deque<Screen> screens;
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;

  public static class Screen {
    private final String name;
    private final Scene scene;

    public Screen(String name, Scene scene) {
      this.name = name;
      this.scene = scene;
    }

    public String getName() {
      return name;
    }

    public Scene getScene() {
      return scene;
    }
  }

  public Navigator(Stage stage) {
    this.screens = new ArrayDeque<>();
  }

  private Screen getCurrentScreen() {
    return screens.peek();
  }

  public void showCurrentScreen() {
    Navigator.Screen currentScreen = this.getCurrentScreen();
    stage.setScene(currentScreen.scene);
    stage.setTitle(currentScreen.name);
    stage.show();
  }

  public void pushScreen(String name, Scene scene) {
    final Screen screen = new Screen(name, scene);
    screens.push(screen);
    this.showCurrentScreen();
  }

  public void popScreen() {
    screens.pop();
    this.showCurrentScreen();
  }

  public void pushReplacement(String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    try {
      final Scene scene = fxmlLoader.load();
      final Screen screen = new Screen(fxml, scene);
      screens.pop();
      screens.push(screen);
      this.showCurrentScreen();
    } catch (Exception e) {

      System.err.println(e.getLocalizedMessage());
    }

  }

  public void pushFXML(String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    try {
      final Scene scene = fxmlLoader.load();
      final Screen screen = new Screen(fxml, scene);
      screens.push(screen);
      this.showCurrentScreen();
    } catch (Exception e) {
      System.err.println(e.getLocalizedMessage());
    }
  }

  public Stage getStage() {
    return this.stage;
  }

  public void changeStage(String fxml) {
    final FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    try {
      final Scene scene = fxmlLoader.load();
      final Screen screen = new Screen(fxml, scene);
      this.screens.clear();
      this.screens.push(screen);
      Stage newStage = new Stage();
      newStage.setWidth(WIDTH);
      newStage.setHeight(HEIGHT);
      this.stage = newStage;
      this.showCurrentScreen();
    } catch (Exception e) {
      System.err.println(e.getLocalizedMessage());
    }

  }
}
