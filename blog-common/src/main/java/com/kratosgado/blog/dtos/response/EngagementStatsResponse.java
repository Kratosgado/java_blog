package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO for user engagement statistics.
 * Provides ranked lists of posts and categories by engagement metrics.
 * Used in analytics dashboards to identify trending content.
 */
@Schema(description = "Engagement statistics including top posts by views/likes and popular categories")
public record EngagementStatsResponse(
    @Schema(description = "List of posts ranked by view count")
    List<PostEngagementSummary> topPostsByViews,

    @Schema(description = "List of posts ranked by likes count")
    List<PostEngagementSummary> topPostsByLikes,

    @Schema(description = "List of categories ranked by post count")
    List<CategorySummaryWithCount> popularCategories) {

  /**
   * Nested DTO representing post engagement metrics.
   * Contains essential fields for ranking posts by user interaction.
   */
  @Schema(description = "Post summary with engagement metrics (views and likes)")
  public record PostEngagementSummary(
      @Schema(description = "Unique post identifier", example = "1")
      Long id,

      @Schema(description = "Post title", example = "Advanced Java Concurrency Patterns")
      String title,

      @Schema(description = "URL-friendly post slug", example = "advanced-java-concurrency-patterns")
      String slug,

      @Schema(description = "Number of post views", example = "543")
      int views,

      @Schema(description = "Number of likes received", example = "87")
      int likesCount) {
  }

  /**
   * Nested DTO representing category popularity metrics.
   * Contains category information with post count for ranking.
   */
  @Schema(description = "Category summary with post count for popularity ranking")
  public record CategorySummaryWithCount(
      @Schema(description = "Unique category identifier", example = "1")
      Long id,

      @Schema(description = "Category name", example = "Technology")
      String name,

      @Schema(description = "URL-friendly category slug", example = "technology")
      String slug,

      @Schema(description = "Number of posts in this category", example = "42")
      int postCount) {
  }
}
