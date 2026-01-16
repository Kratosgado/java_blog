package com.kratosgado.blog.utils.http;

import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.request.ChangePasswordRequest;
import com.kratosgado.blog.dtos.request.UpdateUserAvatarRequest;
import com.kratosgado.blog.dtos.request.UpdateUserProfileRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.models.User;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Client for user REST API endpoints
 */
public class UserApiClient extends BaseApiClient {

  public UserApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get user by ID
   */
  public User getUserById(Long id) throws IOException {
    logger.info("Fetching user by ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.get("/users/" + id, authToken, String.class);

    return handleResponse(response, User.class, "Get user by ID");
  }

  /**
   * Get user by email
   */
  public User getUserByEmail(String email) throws IOException {
    logger.info("Fetching user by email: {}", email);

    HttpClient.HttpResponse<String> response = httpClient.get("/users/email/" + email, authToken, String.class);

    return handleResponse(response, User.class, "Get user by email");
  }

  /**
   * Get user by username
   */
  public User getUserByUsername(String username) throws IOException {
    logger.info("Fetching user by username: {}", username);

    HttpClient.HttpResponse<String> response = httpClient.get("/users/username/" + username, authToken, String.class);

    return handleResponse(response, User.class, "Get user by username");
  }

  /**
   * Get all users with pagination
   */
  public PageResponse<UserResponse> getAllUsers(int page, int size) throws IOException {
    logger.info("Fetching all users - page: {}, size: {}", page, size);

    String endpoint = String.format("/users?page=%d&size=%d", page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageType = new TypeToken<PageResponse<UserResponse>>() {
    }.getType();
    return gson.fromJson(response.getRawBody(), pageType);
  }

  /**
   * Update user profile
   */
  public User updateUserProfile(Long id, UpdateUserProfileRequest request) throws IOException {
    logger.info("Updating user profile ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/users/" + id + "/profile", request, authToken,
        String.class);

    User user = handleResponse(response, User.class, "Update user profile");
    logger.info("User profile updated successfully");
    return user;
  }

  /**
   * Change user password
   */
  public void changePassword(Long id, ChangePasswordRequest request) throws IOException {
    logger.info("Changing password for user ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/users/" + id + "/password", request, authToken,
        String.class);

    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Change password failed: {}", errorMessage);
      throw new ApiException("Change password failed: " + errorMessage, response.getStatusCode());
    }

    logger.info("Password changed successfully");
  }

  /**
   * Update user avatar
   */
  public User updateUserAvatar(Long id, String avatarUrl) throws IOException {
    logger.info("Updating avatar for user ID: {}", id);

    UpdateUserAvatarRequest request = new UpdateUserAvatarRequest(id, avatarUrl);

    HttpClient.HttpResponse<String> response = httpClient.put("/users/" + id + "/avatar", request, authToken,
        String.class);

    User user = handleResponse(response, User.class, "Update user avatar");
    logger.info("User avatar updated successfully");
    return user;
  }
}
