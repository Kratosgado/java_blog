package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
  private ComboBox<String> themeComboBox;

  @FXML
  private ComboBox<String> languageComboBox;

  @FXML
  private Button saveSettingsBtn;

  @FXML
  private Button resetSettingsBtn;

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
      messageLabel.setStyle("-fx-text-fill: #6b7280;");
    } catch (Exception e) {
      logger.error("Error saving settings", e);
      messageLabel.setText("Error: " + e.getMessage());
      messageLabel.setStyle("-fx-text-fill: #1f2937;");
    }
  }

  private void resetSettings() {
    loadSettings();
    messageLabel.setText("Settings reset to default");
    messageLabel.setStyle("-fx-text-fill: #4b5563;");
    logger.info("Settings reset to default");
  }
}
