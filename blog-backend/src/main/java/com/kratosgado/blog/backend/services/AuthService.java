package com.kratosgado.blog.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.models.User;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User login(LoginRequest request) {
    var user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> BlogException.unauthorized("Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw BlogException.unauthorized("Invalid email or password");
    }

    logger.info("User logged in successfully: {}", user.getEmail());
    return user;
  }

  public User register(RegisterRequest request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw BlogException.conflict("Email already exists");
    }

    var user = new User();
    user.setEmail(request.email());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole("USER");

    user = userRepository.save(user);

    logger.info("User registered successfully: {}", user.getEmail());
    return user;
  }
}
