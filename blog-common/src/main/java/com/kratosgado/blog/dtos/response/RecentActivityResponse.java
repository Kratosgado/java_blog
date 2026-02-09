package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for recent platform activity. Provides lists of recently created posts and comments.
 * Used in admin dashboards and activity feeds to monitor platform engagement.
 */
@Schema(description = "Recent activity on the platform including latest posts and comments")
public record RecentActivityResponse(
    @Schema(description = "List of recently published posts") List<RecentPost> latestPosts,

    @Schema(description = "List of recently posted comments") List<RecentComment> latestComments) {

  /**
   * Nested DTO representing a recently created post. Contains minimal information for activity feed
   * display.
   */
  @Schema(description = "Summary of a recently created post")
  public record RecentPost(@Schema(description = "Unique post identifier", example = "1") Long id,

      @Schema(description = "Post title", example = "Understanding Docker Containers") String title,

      @Schema(description = "URL-friendly post slug",
          example = "understanding-docker-containers") String slug,

      @Schema(description = "Post creation timestamp",
          example = "2026-02-05T10:30:00") LocalDateTime createdAt) {
  }

  /**
   * Nested DTO representing a recently posted comment. Contains comment details and author
   * information for activity feed.
   */
  @Schema(description = "Summary of a recently posted comment")
  public record RecentComment(
      @Schema(description = "Unique comment identifier (MongoDB ObjectId)",
          example = "507f1f77bcf86cd799439011") String id,

      @Schema(description = "ID of the post this comment belongs to", example = "1") Long postId,

      @Schema(description = "Name of the comment author", example = "johndoe") String authorName,

      @Schema(description = "Comment text content",
          example = "Great article! Very informative.") String content,

      @Schema(description = "Comment creation timestamp",
          example = "2026-02-05T11:45:00") LocalDateTime createdAt) {
  }
}
