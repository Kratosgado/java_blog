
package com.kratosgado.blog.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dtos.request.LoginDto;
import com.kratosgado.blog.dtos.request.SignUpDto;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidationUtils;
import com.kratosgado.blog.utils.validators.Validator;

public class AuthService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
  private final UserDAO userDAO;

  public AuthService() {
    this.userDAO = new UserDAO();
  }

  public boolean register(SignUpDto dto) {
    Validator.validate(dto);
    if (!dto.password().equals(dto.confirmPassword()))
      throw BlogExceptions.badRequest("Passwords do not match");
    if (userDAO.userEmailExists(dto.email()))
      throw BlogExceptions.conflict("Email already exists");

    String hashedPassword = ValidationUtils.hashPassword(dto.password());
    return userDAO.createUser(new User(dto.username(), hashedPassword, dto.email()));
  }

  public User login(LoginDto dto) {
    Validator.validate(dto);
    User user = userDAO.getUserByEmail(dto.email()).orElseThrow(() -> BlogExceptions.notFound("User not found"));
    if (!ValidationUtils.verifyPassword(dto.password(), user.getPassword()))
      throw BlogExceptions.unauthorized("Invalid email or password");
    return user;
  }

}
