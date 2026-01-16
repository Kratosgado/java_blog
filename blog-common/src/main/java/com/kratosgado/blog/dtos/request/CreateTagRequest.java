package com.kratosgado.blog.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description
) {}
