
package com.kratosgado.blog.services;

import java.util.Optional;

import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dtos.request.ChangePasswordDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidationUtils;

public class UserService {

  private final UserDAO userDAO;

  public UserService() {
    this.userDAO = new UserDAO();
  }

  public Optional<User> getUserById(int id) {
    return userDAO.getUserById(id);
  }

  public Optional<User> getUserByEmail(String email) {
    return userDAO.getUserByEmail(email);
  }

  public boolean changePassword(ChangePasswordDto dto) {
    if (dto.newPassword().equals(dto.confirmNewPassword()))
      throw BlogExceptions.badRequest("Passwords do not match");
    User user = userDAO.getUserById(dto.id()).orElseThrow(() -> BlogExceptions.notFound("User not found"));
    if (!ValidationUtils.hashPassword(dto.oldPassword()).equals(user.getPassword()))
      throw BlogExceptions.unauthorized("Invalid password");
    String hashedPassword = ValidationUtils.hashPassword(dto.newPassword());
    user.setPassword(hashedPassword);
    return userDAO.setUserPassword(dto.id(), hashedPassword);

  }

}
