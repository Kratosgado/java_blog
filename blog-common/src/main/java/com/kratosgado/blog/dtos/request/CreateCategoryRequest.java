package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new blog category.
 * Categories are used to organize blog posts by topic or theme.
 */
@Schema(description = "Request payload for creating a new blog category")
public record CreateCategoryRequest(
  @Schema(description = "Category name", example = "Technology", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  String name,

  @Schema(description = "Category description", example = "Posts about technology trends and innovations")
  String description
) {}
