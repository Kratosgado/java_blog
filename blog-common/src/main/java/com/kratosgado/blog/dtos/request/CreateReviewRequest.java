package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new review for a blog post.
 * Reviews include a rating (1-5 stars), title, and detailed content.
 * Reviews are stored in MongoDB with flexible metadata support.
 */
@Schema(description = "Request payload for creating a new review for a blog post")
public record CreateReviewRequest(
    @Schema(description = "ID of the blog post to review", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Post ID is required")
    Long postId,

    @Schema(description = "Rating score (1-5 stars)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    Integer rating,

    @Schema(description = "Review title", example = "Excellent article on clean code", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 5, maxLength = 100)
    @NotBlank(message = "Review title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    String title,

    @Schema(description = "Detailed review content", example = "This article provides excellent insights into writing clean and maintainable code...", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 10, maxLength = 2000)
    @NotBlank(message = "Review content is required")
    @Size(min = 10, max = 2000, message = "Content must be between 10 and 2000 characters")
    String content
) {}
