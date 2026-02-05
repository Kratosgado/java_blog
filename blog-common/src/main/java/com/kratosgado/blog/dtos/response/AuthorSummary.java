package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Projection interface for author/user summary information.
 * Used in post and comment responses to provide minimal author details.
 * Spring Data JPA uses this for efficient query projections, avoiding N+1 query problems.
 */
@Schema(description = "Author summary information for use in post and comment responses")
public interface AuthorSummary {

  @Schema(description = "Unique user identifier", example = "1")
  Long getId();

  @Schema(description = "Author's username", example = "johndoe")
  String getUsername();

  @Schema(description = "Author's email address", example = "john.doe@example.com")
  String getEmail();

  @Schema(description = "URL to author's avatar image", example = "https://example.com/avatars/johndoe.jpg")
  String getAvatarUrl();
}
