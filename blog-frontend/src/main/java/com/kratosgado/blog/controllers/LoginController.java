package com.kratosgado.blog.controllers;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.User;
import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.utils.Navigator;
import com.kratosgado.blog.utils.Routes;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.notifications.ToastNotification;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class LoginController {
  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

  @FXML
  private TextField emailField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private Button loginButton;
  @FXML
  private Button signUpLink;
  @FXML
  private Label infoLabel;

  private final AuthService authService;

  @Inject
  public LoginController(AuthService authService) {
    this.authService = authService;
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
      // Call new REST API-based login
      User user = authService.login(email, password);
      String token = authService.getCurrentToken();

      // Store authentication in context
      AuthContext.getInstance().setAuthentication(user, token);

      ToastNotification.success("Login successful! Welcome back.");
      logger.info("User logged in successfully: {}", email);
      Navigator.getInstance().goTo(Routes.HOME);
    } catch (Exception ex) {
      logger.error("Login failed for email: {}", email, ex);
      ToastNotification.error(ex.getMessage());
    }
  }

  private void switchToSignUp() {
    try {
      Navigator.getInstance().pushReplacement(Routes.SIGNUP);
    } catch (Exception e) {
      logger.error("Failed to navigate to signup", e);
    }
  }
}
