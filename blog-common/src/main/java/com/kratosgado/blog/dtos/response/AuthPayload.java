package com.kratosgado.blog.dtos.response;

import com.kratosgado.blog.models.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GraphQL authentication payload containing JWT token and full user entity.
 * Used in GraphQL authentication mutations to return both token and complete user profile.
 */
@Schema(description = "GraphQL authentication payload with token and user entity")
public record AuthPayload(
  @Schema(description = "JWT authentication token valid for 24 hours",
          example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  String token,

  @Schema(description = "Complete user entity including profile information")
  User user
) {}
