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

public class LoginController {
  @FXML
  private MFXTextField usernameField;
  @FXML
  private MFXPasswordField passwordField;
  @FXML
  private MFXButton loginButton;
  @FXML
  private MFXButton signUpLink;
  @FXML
  private Label errorLabel;

  private final AuthService authService;

  public LoginController() {
    this.authService = new AuthService();
  }

  @FXML
  private void initialize() {
    errorLabel.setVisible(false);

    loginButton.setOnAction(e -> handleLogin());
    signUpLink.setOnAction(e -> switchToSignUp());
  }

  private void handleLogin() {
    errorLabel.setVisible(false);

    String username = usernameField.getText();
    String password = passwordField.getText();

    try {
      if (authService.login(username, password)) {
        errorLabel.setStyle("-fx-text-fill: #4CAF50;");
        errorLabel.setText("Login successful!");
        errorLabel.setVisible(true);
        // Navigate to main application
        System.out.println("Login successful for user: " + username);
      } else {
        showError("Invalid username or password");
      }
    } catch (IllegalArgumentException ex) {
      showError(ex.getMessage());
    }
  }

  private void switchToSignUp() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
      Parent root = loader.load();
      Stage stage = (Stage) loginButton.getScene().getWindow();
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
