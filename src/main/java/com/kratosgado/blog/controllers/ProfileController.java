
package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.ChangePasswordDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.services.UserService;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ProfileController {
  private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

  @FXML
  private Label usernameLabel;

  @FXML
  private Label emailLabel;

  @FXML
  private Label joinDateLabel;

  @FXML
  private TextArea bioArea;

  @FXML
  private MFXTextField websiteField;

  @FXML
  private MFXTextField locationField;

  @FXML
  private MFXButton changeAvatarBtn;

  @FXML
  private MFXButton changePasswordBtn;

  @FXML
  private MFXButton cancelBtn;

  @FXML
  private MFXButton saveProfileBtn;

  @FXML
  private Label messageLabel;

  @FXML
  private MFXPasswordField currentPasswordField;

  @FXML
  private MFXPasswordField newPasswordField;

  @FXML
  private MFXPasswordField confirmNewPasswordField;

  @FXML
  private Label passwordMessageLabel;

  private User user;
  private UserService userService;

  public ProfileController() {
    userService = new UserService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Profile Controller");
    user = AuthContext.getInstance().getCurrentUser();
    usernameLabel.setText(user.getUsername());
    emailLabel.setText(user.getEmail());

    changeAvatarBtn.setOnAction(e -> changeAvatar());
    changePasswordBtn.setOnAction(e -> changePassword());
    cancelBtn.setOnAction(e -> cancel());
    saveProfileBtn.setOnAction(e -> saveProfile());
  }

  private void changeAvatar() {
    logger.info("Change avatar clicked");
  }

  private void changePassword() {
    try {
      int id = user.getId();
      String oldPassword = currentPasswordField.getText();
      String newPassword = newPasswordField.getText();
      String confirmNewPassword = confirmNewPasswordField.getText();

      if (newPassword.isEmpty() || oldPassword.isEmpty()) {
        passwordMessageLabel.setText("All fields are required");
        passwordMessageLabel.setStyle("-fx-text-fill: #f44336;");
        return;
      }

      if (!newPassword.equals(confirmNewPassword)) {
        passwordMessageLabel.setText("Passwords do not match");
        passwordMessageLabel.setStyle("-fx-text-fill: #f44336;");
        return;
      }

      ChangePasswordDto dto = new ChangePasswordDto(id, oldPassword, newPassword, confirmNewPassword);

      if (userService.changePassword(dto)) {
        passwordMessageLabel.setText("Password changed successfully");
        passwordMessageLabel.setStyle("-fx-text-fill: #4CAF50;");
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmNewPasswordField.clear();
        logger.info("Password changed for user: {}", id);
      } else {
        passwordMessageLabel.setText("Failed to change password");
        passwordMessageLabel.setStyle("-fx-text-fill: #f44336;");
      }
    } catch (Exception e) {
      logger.error("Error changing password", e);
      passwordMessageLabel.setText("Error: " + e.getMessage());
      passwordMessageLabel.setStyle("-fx-text-fill: #f44336;");
    }
  }

  private void cancel() {
    currentPasswordField.clear();
    newPasswordField.clear();
    confirmNewPasswordField.clear();
    logger.info("Cancel password change");
  }

  private void saveProfile() {
    try {
      messageLabel.setText("Profile updated successfully");
      messageLabel.setStyle("-fx-text-fill: #4CAF50;");
      logger.info("Profile saved for user: {}", user.getId());
    } catch (Exception e) {
      logger.error("Error saving profile", e);
      messageLabel.setText("Error: " + e.getMessage());
      messageLabel.setStyle("-fx-text-fill: #f44336;");
    }
  }

}
