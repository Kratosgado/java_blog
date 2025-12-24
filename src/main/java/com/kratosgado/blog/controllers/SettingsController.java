package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class SettingsController {
  private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

  @FXML
  private CheckBox emailNotificationsCheckbox;

  @FXML
  private CheckBox commentNotificationsCheckbox;

  @FXML
  private CheckBox publishNotificationsCheckbox;

  @FXML
  private MFXComboBox<String> themeComboBox;

  @FXML
  private MFXComboBox<String> languageComboBox;

  @FXML
  private MFXButton saveSettingsBtn;

  @FXML
  private MFXButton resetSettingsBtn;

  @FXML
  private Label messageLabel;

  @FXML
  private void initialize() {
    logger.debug("Initializing Settings Controller");
    setupUI();
    loadSettings();
  }

  private void setupUI() {
    themeComboBox.getItems().addAll("Light", "Dark", "Auto");
    languageComboBox.getItems().addAll("English", "Spanish", "French", "German");

    saveSettingsBtn.setOnAction(e -> saveSettings());
    resetSettingsBtn.setOnAction(e -> resetSettings());
  }

  private void loadSettings() {
    try {
      emailNotificationsCheckbox.setSelected(true);
      commentNotificationsCheckbox.setSelected(true);
      publishNotificationsCheckbox.setSelected(false);
      themeComboBox.setValue("Light");
      languageComboBox.setValue("English");
      logger.info("Settings loaded successfully");
    } catch (Exception e) {
      logger.error("Failed to load settings", e);
    }
  }

  private void saveSettings() {
    try {
      logger.info("Saving settings - Theme: {}, Language: {}", themeComboBox.getValue(), languageComboBox.getValue());
      messageLabel.setText("Settings saved successfully!");
      messageLabel.setStyle("-fx-text-fill: #4CAF50;");
    } catch (Exception e) {
      logger.error("Error saving settings", e);
      messageLabel.setText("Error: " + e.getMessage());
      messageLabel.setStyle("-fx-text-fill: #f44336;");
    }
  }

  private void resetSettings() {
    loadSettings();
    messageLabel.setText("Settings reset to default");
    messageLabel.setStyle("-fx-text-fill: #2196F3;");
    logger.info("Settings reset to default");
  }
}
