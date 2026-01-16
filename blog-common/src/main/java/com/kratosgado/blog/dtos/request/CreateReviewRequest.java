package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
    @NotNull(message = "Post ID is required")
    Long postId,

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    Integer rating,

    @NotBlank(message = "Review title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    String title,

    @NotBlank(message = "Review content is required")
    @Size(min = 10, max = 2000, message = "Content must be between 10 and 2000 characters")
    String content
) {}
