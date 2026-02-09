package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.validation.StrongPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for changing a user's password.
 * Requires the user ID, current password for verification, and new password with confirmation.
 * New password must meet strong password requirements.
 */
@Schema(description = "Request payload for changing a user's password")
public record ChangePasswordRequest(
    @Schema(description = "ID of the user changing password", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    Long userId,

    @Schema(description = "Current password for verification", example = "OldP@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Old password is required")
    String oldPassword,

    @Schema(description = "New password (must meet strength requirements)", example = "NewSecureP@ssw0rd456", requiredMode = Schema.RequiredMode.REQUIRED)
    @StrongPassword
    String newPassword,

    @Schema(description = "New password confirmation (must match new password)", example = "NewSecureP@ssw0rd456", requiredMode = Schema.RequiredMode.REQUIRED)
    @StrongPassword
    String confirmNewPassword
) {
}
