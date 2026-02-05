package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO for blog analytics data.
 * Provides overview metrics including post counts, views, and top performing posts.
 * Used in admin and user dashboards for performance insights.
 */
@Schema(description = "Analytics data including post metrics and top performing posts")
public record AnalyticsResponse(
    @Schema(description = "Total number of posts analyzed", example = "156")
    int totalPosts,

    @Schema(description = "Cumulative views across all posts", example = "12543")
    long totalViews,

    @Schema(description = "Average views per post", example = "80.4")
    double averageViews,

    @Schema(description = "List of top performing posts by views and likes")
    List<TopPostData> topPosts) {

  /**
   * Nested DTO representing a top performing post.
   * Contains essential metrics for ranking and display.
   */
  @Schema(description = "Top performing post with engagement metrics")
  public record TopPostData(
      @Schema(description = "Unique post identifier", example = "1")
      Long id,

      @Schema(description = "Post title", example = "Getting Started with Spring Boot")
      String title,

      @Schema(description = "Number of post views", example = "543")
      long views,

      @Schema(description = "Number of likes received", example = "87")
      int likesCount) {
  }
}
