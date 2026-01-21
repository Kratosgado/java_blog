package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.enums.PostStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
    @NotBlank(message = "Title is required") @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters") String title,
    @NotBlank(message = "Content is required") String content,
    String excerpt,
    @NotNull(message = "Category is required") Long categoryId,
    String coverImage,
    @NotBlank(message = "Status is required") PostStatus status) {
}
