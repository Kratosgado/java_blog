package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for user dashboard statistics.
 * Provides aggregated counts of user-generated content and engagement metrics.
 * Used in user profile dashboard to display activity summary.
 */
@Schema(description = "User dashboard statistics including posts, comments, reviews, and views")
public record UserDashboardStatsResponse(
    @Schema(description = "Total number of posts created by the user", example = "15")
    long totalPosts,

    @Schema(description = "Total number of comments made by the user", example = "47")
    long totalComments,

    @Schema(description = "Total number of reviews written by the user", example = "8")
    long totalReviews,

    @Schema(description = "Total cumulative views across all user's posts", example = "2543")
    long totalViews) {
}
