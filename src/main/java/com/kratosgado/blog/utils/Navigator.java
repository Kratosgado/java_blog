package com.kratosgado.blog.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Navigator {
  private static final Logger logger = LoggerFactory.getLogger(Navigator.class);
  private static Navigator instance;

  private Stage stage;
  private final Deque<AppScreen> screens;
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;

  private static class AppScreen {
    private final String name;
    private final Scene scene;

    public AppScreen(String name, Scene scene) {
      this.name = name;
      this.scene = scene;
    }
  }

  public Navigator() {
    this.screens = new ArrayDeque<>();
  }

  public Navigator(Stage stage) {
    this.screens = new ArrayDeque<>();
  }

  public static Navigator getInstance() {
    if (instance == null) {
      instance = new Navigator();
    }
    return instance;
  }

  public void setStage(Stage stage) {
    // cover full screen
    stage.setHeight(Screen.getPrimary().getBounds().getHeight());
    stage.setWidth(Screen.getPrimary().getBounds().getWidth());
    this.stage = stage;
  }

  private AppScreen getCurrentScreen() {
    return screens.peek();
  }

  public void showCurrentScreen() {
    AppScreen currentScreen = this.getCurrentScreen();
    stage.setScene(currentScreen.scene);
    stage.setTitle(currentScreen.name);
    stage.show();
  }

  public void pushScreen(String name, Scene scene) {
    final AppScreen screen = new AppScreen(name, scene);
    screens.push(screen);
    logger.debug("Screen pushed: {}", name);
    this.showCurrentScreen();
  }

  public void popScreen() {
    AppScreen popped = screens.pop();
    logger.debug("Screen popped: {}", popped.name);
    this.showCurrentScreen();
  }

  public void pushReplacement(String fxml) {
    final AppScreen screen = new AppScreen(fxml, new Scene(loadView(fxml)));
    screens.pop();
    screens.push(screen);
    logger.debug("Screen replaced: {}", fxml);
    this.showCurrentScreen();
  }

  private <T> T loadView(String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(ResourceLoader.loadURL("/fxml/" + fxml + ".fxml"));

    try {
      return fxmlLoader.load();
    } catch (Exception e) {
      logger.error("Failed to load FXML: {}", fxml, e);
      return null;
    }
  }

  public Parent getSubScene(String fxml) {
    return loadView(fxml);
  }

  public void goTo(String fxml) {
    Scene scene = new Scene(loadView(fxml));
    final AppScreen screen = new AppScreen(fxml, scene);
    screens.push(screen);
    logger.debug("Navigating to: {}", fxml);
    this.showCurrentScreen();
  }

  public Stage getStage() {
    return this.stage;
  }

  public void changeStage(String fxml) {
    final AppScreen screen = new AppScreen(fxml, loadView(fxml));
    this.screens.clear();
    this.screens.push(screen);
    Stage newStage = new Stage();
    newStage.setWidth(WIDTH);
    newStage.setHeight(HEIGHT);
    this.stage = newStage;
    logger.debug("Stage changed to: {}", fxml);
    this.showCurrentScreen();
  }
}
