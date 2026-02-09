package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing review.
 * All fields are optional - only provided fields will be updated.
 * Allows users to modify their rating, title, or content.
 */
@Schema(description = "Request payload for updating an existing review. All fields are optional.")
public record UpdateReviewRequest(
    @Schema(description = "Updated rating score (1-5 stars)", example = "4", minimum = "1", maximum = "5")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    Integer rating,

    @Schema(description = "Updated review title", example = "Very good article with practical examples", minLength = 5, maxLength = 100)
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    String title,

    @Schema(description = "Updated review content", example = "After revisiting this article, I found even more valuable insights...", minLength = 10, maxLength = 2000)
    @Size(min = 10, max = 2000, message = "Content must be between 10 and 2000 characters")
    String content
) {}
