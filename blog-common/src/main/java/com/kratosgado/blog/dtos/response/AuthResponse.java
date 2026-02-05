package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for authentication operations (login, register).
 * Contains JWT token and essential user information for client-side session management.
 */
@Schema(description = "Authentication response containing JWT token and user details")
public record AuthResponse(
  @Schema(description = "JWT authentication token valid for 24 hours",
          example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  String token,

  @Schema(description = "Unique user identifier", example = "1")
  Long userId,

  @Schema(description = "User's unique username", example = "johndoe")
  String username,

  @Schema(description = "User's email address", example = "john.doe@example.com")
  String email,

  @Schema(description = "User's role in the system", example = "USER", allowableValues = {"USER", "ADMIN", "MODERATOR"})
  String role
) {}
