package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new comment on a blog post.
 * Comments are stored in MongoDB and support threaded discussions.
 */
@Schema(description = "Request payload for creating a new comment on a blog post")
public record CreateCommentRequest(
  @Schema(description = "ID of the blog post to comment on", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Post ID is required")
  Long postId,

  @Schema(description = "Comment content", example = "Great article! Thanks for sharing.", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Content is required")
  String content
) {}
