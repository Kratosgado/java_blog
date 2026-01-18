
package com.kratosgado.blog.dtos.response;

public record StatCountResponse(
    long totalPosts,
    long totalUsers,
    long totalComments,
    long totalTags,
    long totalReviews) {
}
