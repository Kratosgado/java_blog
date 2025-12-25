package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.LoginDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LoginController {
  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

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
    logger.info("Login attempt for email: {}", email);

    try {
      User user = authService.login(new LoginDto(email, password));
      AuthContext.getInstance().setCurrentUser(user);
      infoLabel.setStyle("-fx-text-fill: #4CAF50;");
      infoLabel.setText("Login successful!");
      infoLabel.setVisible(true);
      logger.info("User logged in successfully: {}", email);
      Navigator.getInstance().goTo(Routes.DASHBOARD);
    } catch (Exception ex) {
      logger.error("Login failed for email: {}", email, ex);
      showError(ex.getMessage());
    }
  }

  private void switchToSignUp() {
    try {
      Navigator.getInstance().pushReplacement(Routes.SIGNUP);
    } catch (Exception e) {
      logger.error("Failed to navigate to signup", e);
    }
  }

  private void showError(String message) {
    infoLabel.setStyle("-fx-text-fill: #f44336;");
    infoLabel.setText(message);
    infoLabel.setVisible(true);
  }
}
