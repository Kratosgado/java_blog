package com.kratosgado.blog.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Container class for review-related response DTOs.
 * Reviews are stored in MongoDB and associated with posts.
 * Provides different response types for ratings and review details.
 */
@Schema(description = "Container for review response DTOs")
public class ReviewResponse {

  /**
   * Response DTO for average rating calculation.
   * Used to return aggregated rating data for posts.
   */
  @Schema(description = "Average rating result for a post")
  public record AverageRatingResult(
      @Schema(description = "Average rating value (1.0-5.0)", example = "4.2")
      Double avgRating) {
  }

  /**
   * Response DTO for review details without author information.
   * Used when fetching reviews for a specific user (author already known).
   */
  @Schema(description = "Review details without author information")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ReviewWithoutUser(
      @Schema(description = "Unique review identifier (MongoDB ObjectId)", example = "507f1f77bcf86cd799439011")
      String id,

      @Schema(description = "ID of the post this review belongs to", example = "1")
      Long postId,

      @Schema(description = "Rating value (1-5 stars)", example = "5", minimum = "1", maximum = "5")
      int rating,

      @Schema(description = "Review title or summary", example = "Excellent article!")
      String title,

      @Schema(description = "Detailed review content", example = "This article provided great insights...")
      String content,

      @Schema(description = "Review creation timestamp", example = "2026-02-05T12:15:00")
      LocalDateTime createdAt,

      @Schema(description = "Last update timestamp", example = "2026-02-05T14:30:00")
      LocalDateTime updatedAt,

      @Schema(description = "Whether this review was marked as helpful by others", example = "true")
      boolean helpful) {
  }
}
