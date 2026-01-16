package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    Integer rating,

    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    String title,

    @Size(min = 10, max = 2000, message = "Content must be between 10 and 2000 characters")
    String content
) {}
