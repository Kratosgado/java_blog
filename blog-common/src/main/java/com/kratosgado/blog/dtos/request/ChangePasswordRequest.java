package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.validation.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(
    @NotNull(message = "User ID is required") Long userId,

    @NotBlank(message = "Old password is required") String oldPassword,

    @StrongPassword String newPassword,

    @StrongPassword String confirmNewPassword) {
}
