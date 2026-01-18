
package com.kratosgado.blog.dtos.response;

public record UserDashboardStatsResponse(
    long totalPosts,
    long totalComments,
    long totalReviews,
    long totalViews) {
}
