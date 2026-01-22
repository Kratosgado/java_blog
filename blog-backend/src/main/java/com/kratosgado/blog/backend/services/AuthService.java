package com.kratosgado.blog.backend.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.dao.UserDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.models.User;

@Service
public class AuthService {
  private final UserDAO userDAO;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
    this.userDAO = userDAO;
    this.passwordEncoder = passwordEncoder;
  }

  public User login(LoginRequest request) {
    var user = userDAO.getUserByEmail(request.email())
        .orElseThrow(() -> BlogException.unauthorized("Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw BlogException.unauthorized("Invalid email or password");
    }
    return user;
  }

  public User register(RegisterRequest request) {
    if (userDAO.getUserByEmail(request.email()).isPresent()) {
      throw BlogException.conflict("Email already exists");
    }

    var user = new User();
    user.setEmail(request.email());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));

    return userDAO.createUser(user)
        .orElseThrow(() -> BlogException.internal("Failed to create user"));
  }
}
