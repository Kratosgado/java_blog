
package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.ChangePasswordDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.services.UserService;
import com.kratosgado.blog.services.UploadService;
import com.kratosgado.blog.utils.ImageUtils;
import com.kratosgado.blog.utils.context.AuthContext;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

public class ProfileController {
  private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

  @FXML
  private Label usernameLabel;
  @FXML
  private ImageView avatarImageView;
  @FXML
  private Label emailLabel;
  @FXML
  private Label joinDateLabel;
  @FXML
  private TextArea bioArea;
  @FXML
  private TextField websiteField;
  @FXML
  private TextField locationField;
  @FXML
  private Button changeAvatarBtn;
  @FXML
  private Button changePasswordBtn;
  @FXML
  private Button cancelBtn;
  @FXML
  private Button saveProfileBtn;
  @FXML
  private Label messageLabel;
  @FXML
  private PasswordField currentPasswordField;
  @FXML
  private PasswordField newPasswordField;
  @FXML
  private PasswordField confirmNewPasswordField;
  @FXML
  private Label passwordMessageLabel;

  private User user;
  private UserService userService;
  private UploadService uploadService;

  public ProfileController() {
    userService = new UserService();
    uploadService = new UploadService();
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Profile Controller");
    user = AuthContext.getInstance().getCurrentUser();
    avatarImageView.setImage(ImageUtils.loadImageWithFallback(user.getAvatarUrl()));
    usernameLabel.setText(user.getUsername());
    emailLabel.setText(user.getEmail());
    
    // Load existing profile data
    if (user.getBio() != null) bioArea.setText(user.getBio());
    if (user.getWebsite() != null) websiteField.setText(user.getWebsite());
    if (user.getLocation() != null) locationField.setText(user.getLocation());

    changeAvatarBtn.setOnAction(e -> changeAvatar());
    changePasswordBtn.setOnAction(e -> changePassword());
    cancelBtn.setOnAction(e -> cancel());
    saveProfileBtn.setOnAction(e -> saveProfile());
  }

  private void changeAvatar() {
    try {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Choose Avatar Image");
      fileChooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
      );
      
      java.io.File file = fileChooser.showOpenDialog(changeAvatarBtn.getScene().getWindow());
      if (file != null) {
        String avatarPath = uploadService.uploadFile(file, UploadService.UploadType.AVATAR);
        if (avatarPath != null) {
          if (userService.updateUserAvatar(user.getId(), avatarPath)) {
            user.setAvatarUrl(avatarPath);
            avatarImageView.setImage(ImageUtils.loadImageWithFallback(avatarPath));
            messageLabel.setText("Avatar updated successfully");
            messageLabel.setStyle("-fx-text-fill: #4CAF50;");
            logger.info("Avatar changed for user: {}", user.getId());
          } else {
            messageLabel.setText("Failed to update avatar");
            messageLabel.setStyle("-fx-text-fill: #f44336;");
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error changing avatar", e);
      messageLabel.setText("Error: " + e.getMessage());
      messageLabel.setStyle("-fx-text-fill: #f44336;");
    }
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
      String bio = bioArea.getText();
      String website = websiteField.getText();
      String location = locationField.getText();
      
      if (userService.updateUserProfile(user.getId(), bio, website, location)) {
        // Update user object
        user.setBio(bio);
        user.setWebsite(website);
        user.setLocation(location);
        
        messageLabel.setText("Profile updated successfully");
        messageLabel.setStyle("-fx-text-fill: #4CAF50;");
        logger.info("Profile saved for user: {}", user.getId());
      } else {
        messageLabel.setText("Failed to update profile");
        messageLabel.setStyle("-fx-text-fill: #f44336;");
      }
    } catch (Exception e) {
      logger.error("Error saving profile", e);
      messageLabel.setText("Error: " + e.getMessage());
      messageLabel.setStyle("-fx-text-fill: #f44336;");
    }
  }

}
