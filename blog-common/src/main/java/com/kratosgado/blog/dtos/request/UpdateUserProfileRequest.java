package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a user's profile information.
 * All fields are optional - only provided fields will be updated.
 * Allows users to modify their username, bio, website, and location.
 */
@Schema(description = "Request payload for updating user profile information. All fields are optional.")
public record UpdateUserProfileRequest(
    @Schema(description = "Updated username", example = "john_doe_updated", minLength = 3, maxLength = 50)
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @Schema(description = "User biography or description", example = "Software developer passionate about clean code and best practices", maxLength = 500)
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    String bio,

    @Schema(description = "User's personal or professional website", example = "https://johndoe.dev", maxLength = 200)
    @Size(max = 200, message = "Website must not exceed 200 characters")
    String website,

    @Schema(description = "User's location", example = "San Francisco, CA", maxLength = 100)
    @Size(max = 100, message = "Location must not exceed 100 characters")
    String location
) {
}
