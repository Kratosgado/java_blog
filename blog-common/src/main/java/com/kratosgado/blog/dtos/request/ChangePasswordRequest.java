package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
  @NotNull(message = "User ID is required")
  Long userId,
  
  @NotBlank(message = "Old password is required")
  String oldPassword,
  
  @NotBlank(message = "New password is required")
  @Size(min = 8, message = "New password must be at least 8 characters")
  String newPassword,
  
  @NotBlank(message = "Password confirmation is required")
  String confirmNewPassword
) {
}
