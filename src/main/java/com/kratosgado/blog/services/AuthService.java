
package com.kratosgado.blog.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dtos.request.LoginDto;
import com.kratosgado.blog.dtos.request.SignUpDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.services.UploadService.UploadType;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidationUtils;
import com.kratosgado.blog.utils.validators.ValidatorEngine;

public class AuthService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
  private final UserDAO userDAO;
  private final UploadService uploadService;

  public AuthService() {
    this.userDAO = new UserDAO();
    this.uploadService = new UploadService();
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
      return uploadService.uploadFile(avatarFilePath, UploadType.AVATAR);
    } catch (Exception e) {
      logger.error("Failed to process avatar upload: {}", avatarFilePath, e);
      throw BlogExceptions.badRequest("Failed to process avatar upload: " + e.getMessage());
    }
  }

  public User login(LoginDto dto) {
    ValidatorEngine.validate(dto);
    User user = userDAO.getUserByEmail(dto.email())
        .orElseThrow(() -> BlogExceptions.notFound("Email or password is incorrect"));
    if (!ValidationUtils.verifyPassword(dto.password(), user.getPassword()))
      throw BlogExceptions.unauthorized("Email or password is incorrect");
    return user;
  }

}
