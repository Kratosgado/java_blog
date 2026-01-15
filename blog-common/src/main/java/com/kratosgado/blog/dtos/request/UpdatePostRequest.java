package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
  @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
  String title,
  
  String content,
  String excerpt,
  Long categoryId,
  String coverImage,
  String status
) {}
