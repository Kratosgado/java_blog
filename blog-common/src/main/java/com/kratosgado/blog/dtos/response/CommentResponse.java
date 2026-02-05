package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import com.kratosgado.blog.enums.CommentStatus;

/**
 * Projection interfaces for comment response data.
 * Uses composition pattern to create different views of comment data.
 * Comments are stored in MongoDB, so these interfaces are used for manual
 * projection
 * rather than Spring Data query projections.
 */
@Schema(description = "Collection of comment projection interfaces for different response views")
public interface CommentResponse {

  /**
   * Base interface for comment summary information.
   * Contains essential comment data without relationships.
   */
  @Schema(description = "Basic comment summary without relationships")
  public interface CommentSummary {

    @Schema(description = "Unique comment identifier (MongoDB ObjectId)", example = "507f1f77bcf86cd799439011")
    String getId();

    @Schema(description = "Comment text content (1-5000 characters)", example = "This is a very insightful post. Thank you for sharing!")
    String getContent();

    @Schema(description = "Moderation status of the comment", example = "APPROVED", allowableValues = { "PENDING",
        "APPROVED", "REJECTED", "SPAM" })
    CommentStatus getStatus();

    @Schema(description = "Comment creation timestamp", example = "2026-02-05T11:45:00")
    LocalDateTime getCreatedAt();
  }

  /**
   * Mixin interface for including post identifier.
   * Used when comment needs to be associated with its parent post.
   */
  @Schema(description = "Mixin interface providing post association")
  interface WithPostId {
    @Schema(description = "ID of the post this comment belongs to", example = "1")
    Long getPostId();
  }

  /**
   * Mixin interface for including author information.
   * Denormalized from User entity to avoid cross-database joins.
   */
  @Schema(description = "Mixin interface providing author details")
  interface WithUser {
    @Schema(description = "Name of the comment author", example = "johndoe")
    String getAuthorName();

    @Schema(description = "URL to author's avatar image", example = "https://example.com/avatars/johndoe.jpg")
    String getAuthorAvatarUrl();
  }

  /**
   * Comment view without author information.
   * Used when fetching comments for a specific user (author already known).
   */
  @Schema(description = "Comment summary with post ID but without author information")
  public interface CommentWithoutUser extends CommentSummary, WithPostId {
  }

  /**
   * Comment view without post identifier.
   * Used when fetching comments for a specific post (post ID already known).
   */
  @Schema(description = "Comment summary with author information but without post ID")
  public interface CommentWithoutPostId extends CommentSummary, WithUser {
  }

}
