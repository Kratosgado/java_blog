package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters") @NotBlank(message = "Password is required") String password) {
}
