package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
  @NotNull(message = "Category ID is required")
  Long id,
  
  @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
  String name,
  
  @Size(min = 5, max = 255, message = "Description must be between 5 and 255 characters")
  String description
) {
}
