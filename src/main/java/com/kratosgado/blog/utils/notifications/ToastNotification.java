package com.kratosgado.blog.utils.notifications;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ToastNotification {
  private static final Logger logger = LoggerFactory.getLogger(ToastNotification.class);
  private static ToastNotification instance;
  private final List<Stage> activeToasts;
  private static final double TOAST_WIDTH = 350;
  private static final double TOAST_HEIGHT = 80;
  private static final Duration ANIMATION_DURATION = Duration.millis(300);
  private static final Duration DEFAULT_DURATION = Duration.seconds(3);

  public enum ToastType {
    SUCCESS("#4CAF50", "✓"),
    ERROR("#f44336", "✕"),
    WARNING("#FF9800", "⚠"),
    INFO("#2196F3", "ℹ");

    final String color;
    final String icon;

    ToastType(String color, String icon) {
      this.color = color;
      this.icon = icon;
    }
  }

  private ToastNotification() {
    this.activeToasts = new ArrayList<>();
  }

  public static synchronized ToastNotification getInstance() {
    if (instance == null) {
      instance = new ToastNotification();
    }
    return instance;
  }

  public void show(String message) {
    show(message, ToastType.INFO, DEFAULT_DURATION);
  }

  public void show(String message, ToastType type) {
    show(message, type, DEFAULT_DURATION);
  }

  public void show(String message, ToastType type, Duration duration) {
    Platform.runLater(() -> {
      try {
        Stage toastStage = createToast(message, type, duration);
        positionToast(toastStage);
        
        // Show with animation
        FadeTransition fadeIn = new FadeTransition(ANIMATION_DURATION, toastStage.getScene().getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        toastStage.show();
        activeToasts.add(toastStage);

        logger.debug("Toast shown: {} ({})", message, type);

      } catch (Exception e) {
        logger.error("Failed to show toast", e);
      }
    });
  }

  private Stage createToast(String message, ToastType type, Duration duration) {
    Stage stage = new Stage();
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setResizable(false);
    stage.setAlwaysOnTop(true);

    // Create toast container
    VBox toastContainer = new VBox(5);
    toastContainer.setPadding(new Insets(15));
    toastContainer.setPrefWidth(TOAST_WIDTH);
    toastContainer.setMinHeight(TOAST_HEIGHT);
    toastContainer.setMaxHeight(TOAST_HEIGHT);
    
    // Apply styles based on type
    String bgColor = type.color;
    String textColor = "WHITE";
    
    toastContainer.setStyle(
        "-fx-background-color: " + bgColor + ";" +
        "-fx-background-radius: 8px;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);" +
        "-fx-border-radius: 8px;"
    );

    // Create header with icon and close button
    HBox header = new HBox();
    header.setAlignment(Pos.CENTER_LEFT);
    header.setSpacing(10);
    
    // Icon label
    Label iconLabel = new Label(type.icon);
    iconLabel.setStyle(
        "-fx-font-size: 18px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-fill: " + textColor + ";"
    );
    
    // Message label
    Label messageLabel = new Label(message);
    messageLabel.setStyle(
        "-fx-font-size: 14px;" +
        "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
        "-fx-text-fill: " + textColor + ";" +
        "-fx-wrap-text: true;"
    );
    messageLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(messageLabel, Priority.ALWAYS);

    // Close button
    Button closeButton = new Button("✕");
    closeButton.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-text-fill: " + textColor + ";" +
        "-fx-font-size: 12px;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 2px 6px;" +
        "-fx-background-radius: 3px;"
    );
    closeButton.setOnMouseEntered(e -> closeButton.setStyle(
        "-fx-background-color: rgba(255,255,255,0.2);" +
        "-fx-text-fill: " + textColor + ";" +
        "-fx-font-size: 12px;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 2px 6px;" +
        "-fx-background-radius: 3px;"
    ));
    closeButton.setOnMouseExited(e -> closeButton.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-text-fill: " + textColor + ";" +
        "-fx-font-size: 12px;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 2px 6px;" +
        "-fx-background-radius: 3px;"
    ));
    closeButton.setOnAction(e -> closeToast(stage));

    header.getChildren().addAll(iconLabel, messageLabel, closeButton);
    toastContainer.getChildren().add(header);

    Scene scene = new Scene(toastContainer);
    scene.setFill(Color.TRANSPARENT);
    stage.setScene(scene);

    // Auto-hide after duration
    Timeline autoHide = new Timeline(new KeyFrame(duration, e -> closeToast(stage)));
    autoHide.play();

    // Handle close on stage hide
    stage.setOnHidden(e -> {
      activeToasts.remove(stage);
      repositionRemainingToasts();
    });

    return stage;
  }

  private void positionToast(Stage stage) {
    double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
    double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    
    // Position in top-right corner
    double xOffset = screenWidth - TOAST_WIDTH - 20;
    double yOffset = 20;
    
    // Stack toasts vertically
    yOffset += activeToasts.size() * (TOAST_HEIGHT + 10);
    
    stage.setX(xOffset);
    stage.setY(yOffset);
  }

  private void repositionRemainingToasts() {
    for (int i = 0; i < activeToasts.size(); i++) {
      Stage toast = activeToasts.get(i);
      double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
      double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
      
      double xOffset = screenWidth - TOAST_WIDTH - 20;
      double yOffset = 20 + i * (TOAST_HEIGHT + 10);
      
      toast.setX(xOffset);
      toast.setY(yOffset);
    }
  }

  private void closeToast(Stage stage) {
    FadeTransition fadeOut = new FadeTransition(ANIMATION_DURATION, stage.getScene().getRoot());
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> {
      activeToasts.remove(stage);
      repositionRemainingToasts();
      stage.close();
    });
    fadeOut.play();
  }

  // Convenience methods
  public static void success(String message) {
    getInstance().show(message, ToastType.SUCCESS);
  }

  public static void error(String message) {
    getInstance().show(message, ToastType.ERROR);
  }

  public static void warning(String message) {
    getInstance().show(message, ToastType.WARNING);
  }

  public static void info(String message) {
    getInstance().show(message, ToastType.INFO);
  }

  public static void success(String message, Duration duration) {
    getInstance().show(message, ToastType.SUCCESS, duration);
  }

  public static void error(String message, Duration duration) {
    getInstance().show(message, ToastType.ERROR, duration);
  }

  public static void warning(String message, Duration duration) {
    getInstance().show(message, ToastType.WARNING, duration);
  }

  public static void info(String message, Duration duration) {
    getInstance().show(message, ToastType.INFO, duration);
  }
}