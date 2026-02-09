package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for user authentication. Contains credentials required for user login. */
@Schema(description = "Request payload for user login/authentication")
public record LoginRequest(
    @Schema(
            description = "User's email address",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
    @Schema(
            description = "User's password",
            example = "SecureP@ssw0rd",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @StrongPassword
        String password) {}
