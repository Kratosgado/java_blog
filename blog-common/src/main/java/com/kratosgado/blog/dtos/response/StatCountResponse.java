package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for global platform statistics.
 * Provides aggregated counts of all entities across the platform.
 * Used in admin dashboard and analytics views.
 */
@Schema(description = "Platform-wide statistics including counts of all major entities")
public record StatCountResponse(
    @Schema(description = "Total number of posts in the system", example = "342")
    long totalPosts,

    @Schema(description = "Total number of registered users", example = "128")
    long totalUsers,

    @Schema(description = "Total number of comments across all posts", example = "1547")
    long totalComments,

    @Schema(description = "Total number of tags in the system", example = "67")
    long totalTags,

    @Schema(description = "Total number of reviews across all posts", example = "234")
    long totalReviews) {
}
