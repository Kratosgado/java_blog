package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing blog tag.
 * All fields are optional - only provided fields will be updated.
 */
@Schema(description = "Request payload for updating an existing blog tag. All fields are optional.")
public record UpdateTagRequest(
    @Schema(description = "Updated tag name", example = "spring-boot", minLength = 2, maxLength = 50)
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    String name,

    @Schema(description = "Updated tag description", example = "Posts about Spring Boot framework", maxLength = 255)
    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description
) {}
