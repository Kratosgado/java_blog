package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
  @NotNull(message = "User ID is required")
  Long userId,
  
  @Size(max = 500, message = "Bio must not exceed 500 characters")
  String bio,
  
  @Size(max = 200, message = "Website must not exceed 200 characters")
  String website,
  
  @Size(max = 100, message = "Location must not exceed 100 characters")
  String location
) {
}
