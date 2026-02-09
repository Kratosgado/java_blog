package com.kratosgado.blog.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for tag information.
 * Includes post count for the tag.
 * Used in tag cloud, tag listing, and tag detail views.
 */
@Schema(description = "Tag information with post count")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TagResponse(
    @Schema(description = "Unique tag identifier", example = "1")
    Long id,

    @Schema(description = "Tag name", example = "Java")
    String name,

    @Schema(description = "URL-friendly tag slug", example = "java")
    String slug,

    @Schema(description = "Detailed description of the tag", example = "Posts about Java programming language")
    String description,

    @Schema(description = "Number of posts with this tag", example = "28")
    Integer postCount) {
}
