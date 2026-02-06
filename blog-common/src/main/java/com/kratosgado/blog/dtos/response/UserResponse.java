package com.kratosgado.blog.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kratosgado.blog.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.rest.core.config.Projection;

/**
 * Response DTO for user profile information. Excludes sensitive information like password hash.
 * Used in user profile views and user listing endpoints.
 */
@Schema(description = "User profile information response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Projection(
    name = "user",
    types = {User.class})
public interface UserResponse {
  @Schema(description = "Unique user identifier", example = "1")
  Long getId();

  @Schema(description = "User's unique username (3-50 characters)", example = "johndoe")
  String getUsername();

  @Schema(description = "User's email address", example = "john.doe@example.com")
  String getEmail();

  @Schema(
      description = "URL to user's avatar image",
      example = "https://example.com/avatars/johndoe.jpg")
  String getAvatarUrl();

  @Schema(
      description = "User's biography or description",
      example = "Software developer and tech enthusiast")
  String getBio();

  @Schema(description = "User's personal or professional website", example = "https://johndoe.com")
  String getWebsite();

  @Schema(description = "User's location", example = "San Francisco, CA")
  String getLocation();

  @Schema(
      description = "User's role in the system",
      example = "USER",
      allowableValues = {"USER", "ADMIN", "MODERATOR"})
  String getRole();
}
