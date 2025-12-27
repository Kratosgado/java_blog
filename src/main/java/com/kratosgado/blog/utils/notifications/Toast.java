package com.kratosgado.blog.utils.notifications;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.ResourceLoader;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.materialfx.controls.MFXSimpleNotification;
import io.github.palexdev.materialfx.enums.NotificationPos;
import io.github.palexdev.materialfx.enums.NotificationState;
import io.github.palexdev.materialfx.factories.InsetsFactory;
import io.github.palexdev.materialfx.notifications.MFXNotificationSystem;
import io.github.palexdev.materialfx.notifications.base.INotification;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxresources.fonts.IconDescriptor;
import io.github.palexdev.mfxresources.fonts.IconsProviders;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeRegular;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {
  private static final Logger logger = LoggerFactory.getLogger(Toast.class);
  private static Toast instance;
  private final List<Stage> activeToasts;
  private static final double TOAST_WIDTH = 350;
  private static final double TOAST_HEIGHT = 60;
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

  private Toast() {
    this.activeToasts = new ArrayList<>();
  }

  public static synchronized Toast getInstance() {
    if (instance == null) {
      instance = new Toast();
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
        MFXNotificationSystem.instance().setPosition(NotificationPos.TOP_RIGHT)
            .publish(createNotification(message));

        logger.debug("Toast shown: {} ({})", message, type);

      } catch (Exception e) {
        logger.error("Failed to show toast", e);
      }
    });
  }

  private INotification createNotification(String message) {
    Notify notification = new Notify();
    notification.setContentText(message);
    return notification;
  }

  private static class Notify extends MFXSimpleNotification {
    private final StringProperty headerText = new SimpleStringProperty("Notification Header");
    private final StringProperty contentText = new SimpleStringProperty();

    public Notify() {

      MFXFontIcon fi = new MFXFontIcon();
      IconDescriptor desc = FontAwesomeRegular.BELL;
      fi.setIconsProvider(IconsProviders.FONTAWESOME_REGULAR);
      fi.setDescription(desc.getDescription());
      fi.setSize(16);
      MFXIconWrapper icon = new MFXIconWrapper(fi, 32);
      Label headerLabel = new Label();
      headerLabel.textProperty().bind(headerText);
      MFXIconWrapper readIcon = new MFXIconWrapper("fas-eye", 16, 32);
      ((MFXFontIcon) readIcon.getIcon()).descriptionProperty().bind(Bindings.createStringBinding(
          () -> (getState() == NotificationState.READ) ? "fas-eye" : "fas-eye-slash",
          notificationStateProperty()));
      StackPane.setAlignment(readIcon, Pos.CENTER_RIGHT);
      StackPane placeHolder = new StackPane(readIcon);
      placeHolder.setMaxWidth(Double.MAX_VALUE);
      HBox.setHgrow(placeHolder, Priority.ALWAYS);
      HBox header = new HBox(10, icon, headerLabel, placeHolder);
      header.setAlignment(Pos.CENTER_LEFT);
      header.setPadding(InsetsFactory.of(5, 0, 5, 0));
      header.setMaxWidth(Double.MAX_VALUE);

      Label contentLabel = new Label();
      contentLabel.getStyleClass().add("content");
      contentLabel.textProperty().bind(contentText);
      contentLabel.setWrapText(true);
      contentLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
      contentLabel.setAlignment(Pos.TOP_LEFT);

      MFXButton action1 = new MFXButton("Action 1");
      MFXButton action2 = new MFXButton("Action 2");
      HBox actionsBar = new HBox(15, action1, action2);
      actionsBar.getStyleClass().add("actions-bar");
      actionsBar.setAlignment(Pos.CENTER_RIGHT);
      actionsBar.setPadding(InsetsFactory.all(5));

      BorderPane container = new BorderPane();
      container.getStyleClass().add("notification");
      container.setTop(header);
      container.setCenter(contentLabel);
      container.setBottom(actionsBar);
      container.getStylesheets().add(ResourceLoader.load("/css/notification.css"));
      container.setMinHeight(200);
      container.setMaxWidth(400);

      setContent(container);
    }

    public String getHeaderText() {
      return headerText.get();
    }

    public StringProperty headerTextProperty() {
      return headerText;
    }

    public void setHeaderText(String headerText) {
      this.headerText.set(headerText);
    }

    public String getContentText() {
      return contentText.get();
    }

    public StringProperty contentTextProperty() {
      return contentText;
    }

    public void setContentText(String contentText) {
      this.contentText.set(contentText);
    }
  } // Convenience methods

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
