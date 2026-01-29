package com.kratosgado.blog.services;

import java.io.IOException;

import com.google.inject.Inject;
import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.ChangePasswordRequest;
import com.kratosgado.blog.dtos.request.UpdateUserAvatarRequest;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.UserApiClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private final UserApiClient userApiClient;

  @Inject
  public UserService() {
    this.userApiClient = new UserApiClient(ApiConfig.getBaseUrl());
    // Set auth token from AuthContext
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.userApiClient.setAuthToken(token);
    }
  }

  private void ensureAuthToken() {
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.userApiClient.setAuthToken(token);
    }
  }

  public User getUserById(Long id) {
    ensureAuthToken();
    try {
      return userApiClient.getUserById(id);
    } catch (IOException e) {
      logger.error("Failed to get user due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get user: {}", e.getMessage());
      throw new RuntimeException("Failed to get user: " + e.getMessage(), e);
    }
  }

  public User getUserByEmail(String email) {
    ensureAuthToken();
    try {
      return userApiClient.getUserByEmail(email);
    } catch (IOException e) {
      logger.error("Failed to get user by email due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get user by email: {}", e.getMessage());
      throw new RuntimeException("Failed to get user by email: " + e.getMessage(), e);
    }
  }

  public User getUserByUsername(String username) {
    ensureAuthToken();
    try {
      return userApiClient.getUserByUsername(username);
    } catch (IOException e) {
      logger.error("Failed to get user by username due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get user by username: {}", e.getMessage());
      throw new RuntimeException("Failed to get user by username: " + e.getMessage(), e);
    }
  }

  public PageResponse<User> getAllUsers(int page, int size) {
    ensureAuthToken();
    try {
      return userApiClient.getAllUsers(page, size);
    } catch (IOException e) {
      logger.error("Failed to get users due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get users: {}", e.getMessage());
      throw new RuntimeException("Failed to get users: " + e.getMessage(), e);
    }
  }

  public User updateUserProfile(Long userId, UpdateUserProfileRequest request) {
    ensureAuthToken();
    try {
      return userApiClient.updateUserProfile(userId, request);
    } catch (IOException e) {
      logger.error("Failed to update user profile due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to update user profile: {}", e.getMessage());
      throw new RuntimeException("Failed to update user profile: " + e.getMessage(), e);
    }
  }

  public void changePassword(ChangePasswordRequest request) {
    ensureAuthToken();
    try {
      userApiClient.changePassword(request.userId(), request);
    } catch (IOException e) {
      logger.error("Failed to change password due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to change password: {}", e.getMessage());
      throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
    }
  }

  public User updateUserAvatar(UpdateUserAvatarRequest request) {
    ensureAuthToken();
    try {
      return userApiClient.updateUserAvatar(request.userId(), request.avatarUrl());
    } catch (IOException e) {
      logger.error("Failed to update user avatar due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to update user avatar: {}", e.getMessage());
      throw new RuntimeException("Failed to update user avatar: " + e.getMessage(), e);
    }
  }
}
