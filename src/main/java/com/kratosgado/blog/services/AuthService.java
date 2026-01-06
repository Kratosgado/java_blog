
package com.kratosgado.blog.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dtos.request.LoginDto;
import com.kratosgado.blog.dtos.request.SignUpDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidationUtils;
import com.kratosgado.blog.utils.validators.ValidatorEngine;

public class AuthService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
  private final UserDAO userDAO;

  public AuthService() {
    this.userDAO = new UserDAO();
  }

  public boolean register(SignUpDto dto) {
    ValidatorEngine.validate(dto);
    if (!dto.password().equals(dto.confirmPassword()))
      throw BlogExceptions.badRequest("Passwords do not match");
    if (userDAO.userEmailExists(dto.email()))
      throw BlogExceptions.conflict("Email already exists");

    String hashedPassword = ValidationUtils.hashPassword(dto.password());
    String avatarUrl = processAvatarUpload(dto.avatarUrl());
    return userDAO.createUser(User.builder()
        .username(dto.username())
        .password(hashedPassword)
        .email(dto.email())
        .avatarUrl(avatarUrl)
        .build());
  }

  private String processAvatarUpload(String avatarFilePath) {
    if (avatarFilePath == null || avatarFilePath.trim().isEmpty()) {
      return null; // No avatar uploaded
    }

    try {
      // Create uploads directory if it doesn't exist
      Path uploadDir = Paths.get("uploads/avatars");
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }

      // Generate unique filename
      String originalFileName = Paths.get(avatarFilePath).getFileName().toString();
      String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
      String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

      // Copy file to uploads directory
      Path source = Paths.get(avatarFilePath);
      Path target = uploadDir.resolve(uniqueFileName);
      Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

      logger.info("Avatar uploaded: {} -> {}", originalFileName, uniqueFileName);
      return "uploads/avatars/" + uniqueFileName;

    } catch (IOException e) {
      logger.error("Failed to process avatar upload: {}", avatarFilePath, e);
      throw BlogExceptions.badRequest("Failed to process avatar upload");
    }
  }

  public User login(LoginDto dto) {
    ValidatorEngine.validate(dto);
    User user = userDAO.getUserByEmail(dto.email()).orElseThrow(() -> BlogExceptions.notFound("User not found"));
    if (!ValidationUtils.verifyPassword(dto.password(), user.getPassword()))
      throw BlogExceptions.unauthorized("Invalid email or password");
    return user;
  }

}
