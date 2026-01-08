package com.kratosgado.blog.controllers;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.SignUpDto;
import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.services.UploadService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.notifications.ToastNotification;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class SignUpController {
  private static final Logger logger = LoggerFactory.getLogger(SignUpController.class);

  @FXML
  private TextField usernameField;
  @FXML
  private TextField emailField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private PasswordField confirmPasswordField;
  @FXML
  private Button signUpButton;
  @FXML
  private Button loginLink;
  @FXML
  private Label infoLabel;
  @FXML
  private ImageView avatarImageView;
  @FXML
  private Button uploadAvatarButton;
  @FXML
  private Label fileNameLabel;

  private String avatarFilePath;

  private final AuthService authService;
  private final UploadService uploadService;

  public SignUpController() {
    this.authService = new AuthService();
    this.uploadService = new UploadService();
  }

  @FXML
  private void initialize() {
    infoLabel.setVisible(false);
    avatarFilePath = null;

    // Set up event handlers
    signUpButton.setOnAction(e -> handleSignUp());
    loginLink.setOnAction(e -> switchToLogin());
    uploadAvatarButton.setOnAction(e -> handleAvatarUpload());

    // Set default avatar (CSS gradient background is used as fallback)
    avatarImageView.setImage(null); // Will use CSS background
  }

  private void handleSignUp() {
    infoLabel.setVisible(false);

    String username = usernameField.getText();
    String email = emailField.getText();
    String password = passwordField.getText();
    String confirmPassword = confirmPasswordField.getText();

    try {
      if (authService.register(new SignUpDto(username, email, password, confirmPassword, avatarFilePath))) {
        ToastNotification.success("Registration successful! Redirecting to login...");
        logger.info("User registered successfully: {}", email);

        PauseTransition pause = new PauseTransition(Duration.millis(1500));
        pause.setOnFinished(event -> switchToLogin());
        pause.play();
      }
    } catch (IllegalArgumentException ex) {
      logger.error("Registration failed for email: {}", email, ex);
      ToastNotification.error(ex.getMessage());
    }
  }

  private void handleAvatarUpload() {
    File selectedFile = uploadService.chooseImageFile(Navigator.getInstance().getStage(), "Choose Profile Picture");
    
    if (selectedFile != null) {
      try {
        // Validate file using UploadService
        uploadService.validateAvatarFile(selectedFile);

        // Load and display image
        Image image = new Image(selectedFile.toURI().toString());
        avatarImageView.setImage(image);
        avatarFilePath = selectedFile.getAbsolutePath();
        fileNameLabel.setText(selectedFile.getName());

        ToastNotification.success("Profile picture uploaded successfully");

      } catch (Exception e) {
        logger.error("Failed to load profile picture", e);
        ToastNotification.error(e.getMessage());
      }
    }
  }

  private void switchToLogin() {
    try {
      Navigator.getInstance().pushReplacement(Routes.LOGIN);
    } catch (Exception e) {
      logger.error("Failed to navigate to login", e);
    }
  }

  private void showError(String message) {
    infoLabel.setStyle("-fx-text-fill: #f44336;");
    infoLabel.setText(message);
    infoLabel.setVisible(true);
  }
}
