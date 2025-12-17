
package com.kratosgado.blog.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

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

    String hashedPassword = hashPassword(password);
    return userDAO.createUser(new User(username, hashedPassword, email));
  }

  public boolean login(String email, String password) {
    if (email == null || email.trim().isEmpty())
      throw BlogExceptions.badRequest("Email cannot be empty");
    if (password == null || password.trim().isEmpty())
      throw BlogExceptions.badRequest("Password cannot be empty");
    Optional<User> user = userDAO.getUserByEmail(email);
    if (user.isEmpty())
      throw BlogExceptions.notFound("User not found");
    return user.get().getPassword().equals(hashPassword(password));
  }

  private String hashPassword(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(password.getBytes());
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1)
          hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw BlogExceptions.internal();
    }
  }

}
