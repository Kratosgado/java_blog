package com.kratosgado.blog.controllers;

import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.utils.Navigator;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LoginController {
  @FXML
  private MFXTextField emailField;
  @FXML
  private MFXPasswordField passwordField;
  @FXML
  private MFXButton loginButton;
  @FXML
  private MFXButton signUpLink;
  @FXML
  private Label infoLabel;

  private final AuthService authService;

  public LoginController() {
    this.authService = new AuthService();
  }

  @FXML
  private void initialize() {
    infoLabel.setVisible(false);

    loginButton.setOnAction(e -> handleLogin());
    signUpLink.setOnAction(e -> switchToSignUp());
  }

  private void handleLogin() {
    infoLabel.setVisible(false);

    String email = emailField.getText();
    String password = passwordField.getText();

    try {
      if (authService.login(email, password)) {
        infoLabel.setStyle("-fx-text-fill: #4CAF50;");
        infoLabel.setText("Login successful!");
        infoLabel.setVisible(true);
        // Navigate to main application
        System.out.println("Login successful for user: " + email);
      } else {
        showError("Invalid email or password");
      }
    } catch (IllegalArgumentException ex) {
      showError(ex.getMessage());
    }
  }

  private void switchToSignUp() {
    try {
      Navigator.getInstance().pushReplacement("signup");
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
