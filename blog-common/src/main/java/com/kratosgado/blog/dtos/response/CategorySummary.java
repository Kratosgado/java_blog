package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Projection interface for category summary information.
 * Used in post responses to provide minimal category details without circular references.
 * Spring Data JPA uses this for efficient query projections.
 */
@Schema(description = "Category summary information for use in post responses")
public interface CategorySummary {

  @Schema(description = "Unique category identifier", example = "1")
  Long getId();

  @Schema(description = "Category name", example = "Technology")
  String getName();

  @Schema(description = "URL-friendly category slug", example = "technology")
  String getSlug();
}
