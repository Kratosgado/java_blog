
package com.kratosgado.blog.services;

import java.util.Optional;

import com.google.inject.Inject;
import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dtos.request.ChangePasswordDto;
import com.kratosgado.blog.dtos.request.UpdateUserAvatarDto;
import com.kratosgado.blog.dtos.request.UpdateUserProfileDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidationUtils;
import com.kratosgado.blog.utils.validators.ValidatorEngine;

public class UserService {

  private final UserDAO userDAO;

  @Inject
  public UserService(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  public Optional<User> getUserById(int id) {
    return userDAO.getUserById(id);
  }

  public Optional<User> getUserByEmail(String email) {
    return userDAO.getUserByEmail(email);
  }

  public boolean changePassword(ChangePasswordDto dto) {
    ValidatorEngine.validate(dto);
    if (!dto.newPassword().equals(dto.confirmNewPassword()))
      throw BlogExceptions.badRequest("Passwords do not match");
    User user = userDAO.getUserById(dto.id()).orElseThrow(() -> BlogExceptions.notFound("User not found"));
    if (!ValidationUtils.verifyPassword(dto.oldPassword(), user.getPassword()))
      throw BlogExceptions.unauthorized("Invalid password");
    String hashedPassword = ValidationUtils.hashPassword(dto.newPassword());
    user.setPassword(hashedPassword);
    return userDAO.setUserPassword(dto.id(), hashedPassword);

  }

  public boolean updateUserProfile(UpdateUserProfileDto dto) {
    ValidatorEngine.validate(dto);
    if (userDAO.getUserById(dto.userId()).isEmpty()) {
      throw BlogExceptions.notFound("User not found");
    }
    return userDAO.updateUserProfile(dto.userId(), dto.bio(), dto.website(), dto.location());
  }

  public boolean updateUserAvatar(UpdateUserAvatarDto dto) {
    ValidatorEngine.validate(dto);
    if (userDAO.getUserById(dto.userId()).isEmpty()) {
      throw BlogExceptions.notFound("User not found");
    }
    return userDAO.updateUserAvatar(dto.userId(), dto.avatarUrl());
  }

}
