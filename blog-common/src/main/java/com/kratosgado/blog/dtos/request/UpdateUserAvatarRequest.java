package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserAvatarRequest(
  @NotNull(message = "User ID is required")
  Long userId,
  
  @NotBlank(message = "Avatar URL is required")
  String avatarUrl
) {
}
