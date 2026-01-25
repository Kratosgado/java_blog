package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @StrongPassword String password) {
}
