
package com.kratosgado.blog.services;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.http.AuthApiClient;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;

import java.io.IOException;

/**
 * Service for handling authentication via REST API
 */
public class AuthService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
  private final AuthApiClient authApiClient;
  private String currentToken;
  private User currentUser;

  @Inject
  public AuthService() {
    this.authApiClient = new AuthApiClient(ApiConfig.getBaseUrl());
  }

  /**
   * Register a new user
   * @param username Username
   * @param email Email
   * @param password Password
   * @param confirmPassword Password confirmation
   * @return User object with auth token stored
   */
  public User register(String username, String email, String password, String confirmPassword) {
    try {
      // Validate passwords match
      if (!password.equals(confirmPassword)) {
        throw new IllegalArgumentException("Passwords do not match");
      }

      // Create register request
      RegisterRequest request = new RegisterRequest(username, email, password);
      
      // Call API
      AuthResponse response = authApiClient.register(request);
      
      // Store token and user info
      this.currentToken = response.token();
      this.currentUser = User.builder()
        .id(response.userId())
        .username(response.username())
        .email(response.email())
        .role(response.role())
        .build();
      
      logger.info("User registered successfully: {}", email);
      return currentUser;
      
    } catch (IOException e) {
      logger.error("Registration failed due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Registration failed: {}", e.getMessage());
      throw new RuntimeException("Registration failed: " + e.getMessage(), e);
    }
  }

  /**
   * Login with email and password
   * @param email User email
   * @param password User password
   * @return User object with auth token stored
   */
  public User login(String email, String password) {
    try {
      // Create login request
      LoginRequest request = new LoginRequest(email, password);
      
      // Call API
      AuthResponse response = authApiClient.login(request);
      
      // Store token and user info
      this.currentToken = response.token();
      this.currentUser = User.builder()
        .id(response.userId())
        .username(response.username())
        .email(response.email())
        .role(response.role())
        .build();
      
      logger.info("User logged in successfully: {}", email);
      return currentUser;
      
    } catch (IOException e) {
      logger.error("Login failed due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Login failed: {}", e.getMessage());
      throw new RuntimeException("Login failed: " + e.getMessage(), e);
    }
  }

  /**
   * Validate current token
   * @return true if token is valid
   */
  public boolean validateToken() {
    if (currentToken == null || currentToken.isEmpty()) {
      return false;
    }
    return authApiClient.validateToken(currentToken);
  }

  /**
   * Logout current user
   */
  public void logout() {
    this.currentToken = null;
    this.currentUser = null;
    logger.info("User logged out");
  }

  /**
   * Get current authentication token
   */
  public String getCurrentToken() {
    return currentToken;
  }

  /**
   * Get current user
   */
  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Check if user is authenticated
   */
  public boolean isAuthenticated() {
    return currentToken != null && currentUser != null && validateToken();
  }
}
