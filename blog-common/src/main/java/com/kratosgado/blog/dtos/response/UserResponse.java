package com.kratosgado.blog.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for user profile information.
 * Excludes sensitive information like password hash.
 * Used in user profile views and user listing endpoints.
 */
@Schema(description = "User profile information response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    @Schema(description = "Unique user identifier", example = "1")
    Long id,

    @Schema(description = "User's unique username (3-50 characters)", example = "johndoe")
    String username,

    @Schema(description = "User's email address", example = "john.doe@example.com")
    String email,

    @Schema(description = "URL to user's avatar image", example = "https://example.com/avatars/johndoe.jpg")
    String avatarUrl,

    @Schema(description = "User's biography or description", example = "Software developer and tech enthusiast")
    String bio,

    @Schema(description = "User's personal or professional website", example = "https://johndoe.com")
    String website,

    @Schema(description = "User's location", example = "San Francisco, CA")
    String location,

    @Schema(description = "User's role in the system", example = "USER", allowableValues = {"USER", "ADMIN", "MODERATOR"})
    String role) {
}
