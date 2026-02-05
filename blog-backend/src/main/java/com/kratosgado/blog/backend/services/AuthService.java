package com.kratosgado.blog.backend.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.InvalidRequestException;
import com.kratosgado.blog.backend.exceptions.ResourceAlreadyExistsException;
import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User login(LoginRequest request) {
    var user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new UnauthorizedException("Invalid email or password");
    }
    return user;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public User register(RegisterRequest request) {
    if (!request.password().equals(request.confirmPassword())) {
      throw new InvalidRequestException("Passwords do not match");
    }
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new ResourceAlreadyExistsException("Email already exists");
    }

    var user = new User();
    user.setEmail(request.email());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setAvatarUrl(request.avatarUrl());
    return userRepository.save(user);
  }
}
