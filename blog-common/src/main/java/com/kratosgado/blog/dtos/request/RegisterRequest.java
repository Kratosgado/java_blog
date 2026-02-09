package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.validation.StrongPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 * Contains all necessary information to create a new user account including
 * username, email, password, and optional avatar URL.
 */
@Schema(description = "Request payload for user registration")
public record RegisterRequest(
    @Schema(description = "Unique username for the account", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @Schema(description = "User's email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @Schema(description = "URL or path to user's avatar image", example = "https://example.com/avatars/johndoe.jpg")
    String avatarUrl,

    @Schema(description = "User's password (must meet strength requirements)", example = "SecureP@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
    @StrongPassword
    String password,

    @Schema(description = "Password confirmation (must match password)", example = "SecureP@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
    @StrongPassword
    String confirmPassword
) {

}
