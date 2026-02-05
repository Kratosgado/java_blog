package com.kratosgado.blog.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for category information.
 * Includes post count for the category.
 * Used in category listing and detail views.
 */
@Schema(description = "Category information with post count")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponse(
    @Schema(description = "Unique category identifier", example = "1")
    Long id,

    @Schema(description = "Category name", example = "Technology")
    String name,

    @Schema(description = "URL-friendly category slug", example = "technology")
    String slug,

    @Schema(description = "Detailed description of the category", example = "Articles about technology, software, and innovation")
    String description,

    @Schema(description = "Number of posts in this category", example = "42")
    Long postCount) {
}
