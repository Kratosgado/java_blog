
package com.kratosgado.blog.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.ValidationUtils;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;

public class AuthService {
  private final UserDAO userDAO;

  public AuthService() {
    this.userDAO = new UserDAO();
  }

  public boolean register(String username, String email, String password) {
    if (username == null || username.trim().isEmpty())
      throw BlogExceptions.badRequest("Username cannot be empty");
    if (email == null || email.trim().isEmpty())
      throw BlogExceptions.badRequest("Email cannot be empty");
    if (!ValidationUtils.isValidEmail(email))
      throw BlogExceptions.badRequest("Invalid email");
    if (password == null || password.trim().isEmpty())
      throw BlogExceptions.badRequest("Password cannot be empty");
    if (userDAO.userEmailExists(email))
      throw BlogExceptions.conflict("Email already exists");

    String hashedPassword = ValidationUtils.hashPassword(password);
    return userDAO.createUser(new User(username, hashedPassword, email));
  }

  public User login(String email, String password) {
    if (email == null || email.trim().isEmpty())
      throw BlogExceptions.badRequest("Email cannot be empty");
    if (password == null || password.trim().isEmpty())
      throw BlogExceptions.badRequest("Password cannot be empty");
    User user = userDAO.getUserByEmail(email).orElseThrow(() -> BlogExceptions.notFound("User not found"));
    if (!user.getPassword().equals(ValidationUtils.hashPassword(password)))
      throw BlogExceptions.unauthorized("Invalid email or password");
    return user;
  }

}
