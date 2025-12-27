package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.SignUpDto;
import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.notifications.Toast;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class SignUpController {
  private static final Logger logger = LoggerFactory.getLogger(SignUpController.class);

  @FXML
  private MFXTextField usernameField;
  @FXML
  private MFXTextField emailField;
  @FXML
  private MFXPasswordField passwordField;
  @FXML
  private MFXPasswordField confirmPasswordField;
  @FXML
  private MFXButton signUpButton;
  @FXML
  private MFXButton loginLink;
  @FXML
  private Label infoLabel;

  private final AuthService authService;

  public SignUpController() {
    this.authService = new AuthService();
  }

  @FXML
  private void initialize() {
    infoLabel.setVisible(false);

    signUpButton.setOnAction(e -> handleSignUp());
    loginLink.setOnAction(e -> switchToLogin());
  }

  private void handleSignUp() {
    infoLabel.setVisible(false);

    String username = usernameField.getText();
    String email = emailField.getText();
    String password = passwordField.getText();
    String confirmPassword = confirmPasswordField.getText();

    try {
      if (authService.register(new SignUpDto(username, email, password, confirmPassword))) {
        Toast.success("Registration successful! Redirecting to login...");
        logger.info("User registered successfully: {}", email);

        PauseTransition pause = new PauseTransition(Duration.millis(1500));
        pause.setOnFinished(event -> switchToLogin());
        pause.play();
      }
    } catch (IllegalArgumentException ex) {
      logger.error("Registration failed for email: {}", email, ex);
      Toast.error(ex.getMessage());
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
