package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Projection interface for tag summary information.
 * Used in post responses to provide minimal tag details without circular references.
 * Spring Data JPA uses this for efficient query projections.
 */
@Schema(description = "Tag summary information for use in post responses")
public interface TagSummary {

  @Schema(description = "Unique tag identifier", example = "1")
  Long getId();

  @Schema(description = "Tag name", example = "Java")
  String getName();

  @Schema(description = "URL-friendly tag slug", example = "java")
  String getSlug();
}
