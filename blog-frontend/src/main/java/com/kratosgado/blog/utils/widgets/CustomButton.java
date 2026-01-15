
package com.kratosgado.blog.utils.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class CustomButton extends Button {

  public CustomButton(String text, ButtonType type) {
    super(text);
    getStyleClass().add(type.getStyleClass());
  }

  public CustomButton(String text, EventHandler<ActionEvent> onClick) {
    this(text, ButtonType.PRIMARY);
    setOnAction(onClick);
  }

  public CustomButton(String text, ButtonType type, EventHandler<ActionEvent> onClick) {
    this(text, type);
    setOnAction(onClick);
  }

  public enum ButtonType {
    PRIMARY("primary-button"),
    SECONDARY("secondary-button"),
    TERTIARY("tertiary-button"),
    ERROR("error-button"),
    SUCCESS("success-button"),
    WARNING("warning-button"),
    INFO("info-button"),
    LIGHT("light-button"),
    DARK("dark-button");

    private final String styleClass;

    ButtonType(String styleClass) {
      this.styleClass = styleClass;
    }

    public String getStyleClass() {
      return styleClass;
    }
  }

}
