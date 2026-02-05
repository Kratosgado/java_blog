package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing blog category.
 * Requires category ID and accepts optional name and description updates.
 */
@Schema(description = "Request payload for updating an existing blog category")
public record UpdateCategoryRequest(
  @Schema(description = "ID of the category to update", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Category ID is required")
  Long id,

  @Schema(description = "Updated category name", example = "Software Engineering", minLength = 2, maxLength = 50)
  @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
  String name,

  @Schema(description = "Updated category description", example = "Posts about software development and engineering practices", minLength = 5, maxLength = 255)
  @Size(min = 5, max = 255, message = "Description must be between 5 and 255 characters")
  String description
) {
}
