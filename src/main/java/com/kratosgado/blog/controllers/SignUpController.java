package com.kratosgado.blog.controllers;

import com.kratosgado.blog.services.AuthService;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
  private Label errorLabel;

  private final AuthService authService;

  public SignUpController() {
    this.authService = new AuthService();
  }

  @FXML
  private void initialize() {
    errorLabel.setVisible(false);

    signUpButton.setOnAction(e -> handleSignUp());
    loginLink.setOnAction(e -> switchToLogin());
  }

  private void handleSignUp() {
    errorLabel.setVisible(false);

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
        errorLabel.setStyle("-fx-text-fill: #4CAF50;");
        errorLabel.setText("Registration successful! Redirecting to login...");
        errorLabel.setVisible(true);

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
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
      Parent root = loader.load();
      Stage stage = (Stage) signUpButton.getScene().getWindow();
      stage.setScene(new Scene(root));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showError(String message) {
    errorLabel.setStyle("-fx-text-fill: #f44336;");
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }
}
