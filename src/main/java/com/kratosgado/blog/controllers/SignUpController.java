package com.kratosgado.blog.controllers;

import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SignUpController {
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

    if (!password.equals(confirmPassword)) {
      showError("Passwords do not match");
      return;
    }

    try {
      if (authService.register(username, email, password)) {
        infoLabel.setStyle("-fx-text-fill: #4CAF50;");
        infoLabel.setText("Registration successful! Redirecting to login...");
        infoLabel.setVisible(true);

        // Delay before switching to login
        new Thread(() -> {
          try {
            Thread.sleep(1500);
            javafx.application.Platform.runLater(this::switchToLogin);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }).start();
      }
    } catch (IllegalArgumentException ex) {
      showError(ex.getMessage());
    }
  }

  private void switchToLogin() {
    try {
      Navigator.getInstance().pushReplacement(Routes.LOGIN);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showError(String message) {
    infoLabel.setStyle("-fx-text-fill: #f44336;");
    infoLabel.setText(message);
    infoLabel.setVisible(true);
  }
}
