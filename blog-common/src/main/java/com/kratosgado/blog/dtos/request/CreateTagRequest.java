package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new blog tag.
 * Tags are used to label and categorize blog posts for easier discovery and filtering.
 */
@Schema(description = "Request payload for creating a new blog tag")
public record CreateTagRequest(
    @Schema(description = "Tag name", example = "java", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    String name,

    @Schema(description = "Tag description", example = "Posts about Java programming language", maxLength = 255)
    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description
) {}
