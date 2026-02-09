package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating a user's avatar image.
 * Requires the user ID and new avatar URL.
 */
@Schema(description = "Request payload for updating a user's avatar image")
public record UpdateUserAvatarRequest(
  @Schema(description = "ID of the user updating avatar", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "User ID is required")
  Long userId,

  @Schema(description = "URL or path to the new avatar image", example = "https://example.com/avatars/new-avatar.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Avatar URL is required")
  String avatarUrl
) {
}
