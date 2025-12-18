
package com.kratosgado.blog.controllers;

import com.kratosgado.blog.models.User;
import com.kratosgado.blog.services.UserService;
import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dtos.request.ChangePasswordDto;
import com.kratosgado.blog.utils.context.AuthContext;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ProfileController {

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
    user = AuthContext.getInstance().getCurrentUser();
    usernameLabel.setText(user.getUsername());
    emailLabel.setText(user.getEmail());

    changeAvatarBtn.setOnAction(e -> changeAvatar());
    changePasswordBtn.setOnAction(e -> changePassword());
    cancelBtn.setOnAction(e -> cancel());
    saveProfileBtn.setOnAction(e -> saveProfile());
    // joinDateLabel.setText(user.getJoinDate().toString());
    // bioArea.setText(user.getBio());
    // websiteLabel.setText(user.getWebsite());
    // locationLabel.setText(user.getLocation());

    // changePasswordScrollPane.setVisible(false);
    // profileCard.setVisible(true);
    // changePasswordCard.setVisible(false);
  }

  private void changeAvatar() {
    // changePasswordScrollPane.setVisible(true);
    // profileCard.setVisible(false);
    // changePasswordCard.setVisible(true);
  }

  private void changePassword() {
    int id = user.getId();
    String oldPassword = currentPasswordField.getText();
    String newPassword = newPasswordField.getText();
    String confirmNewPassword = confirmNewPasswordField.getText();
    if (!newPassword.equals(confirmNewPassword)) {
      passwordMessageLabel.setText("Passwords do not match");
      return;
    }
    ChangePasswordDto dto = new ChangePasswordDto(id, oldPassword, newPassword, confirmNewPassword);

    if (userService.changePassword(dto)) {
      passwordMessageLabel.setText("Password changed successfully");
    } else {
      passwordMessageLabel.setText("Failed to change password");
    }
  }

  private void cancel() {
    // changePasswordScrollPane.setVisible(false);
    // profileCard.setVisible(true);
    // changePasswordCard.setVisible(false);
  }

  private void saveProfile() {
    // user.setUsername(usernameField.getText());
    // user.setEmail(emailField.getText());
    // user.setBio(bioArea.getText());
    // user.setWebsite(websiteField.getText());
    // user.setLocation(locationField.getText());

    // AuthContext.getInstance().updateUser(user);

    // changePasswordScrollPane.setVisible(false);
    // profileCard.setVisible(true);
    // changePasswordCard.setVisible(false);
  }

}
