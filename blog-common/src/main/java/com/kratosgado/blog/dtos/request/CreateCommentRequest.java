package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
  @NotNull(message = "Post ID is required")
  Long postId,
  
  @NotBlank(message = "Content is required")
  String content
) {}
