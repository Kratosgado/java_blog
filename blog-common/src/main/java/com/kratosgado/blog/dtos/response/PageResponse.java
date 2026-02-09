package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Generic paginated response wrapper for API endpoints.
 * Provides pagination metadata along with the actual content list.
 * Used for paginated GET requests across posts, comments, tags, categories, etc.
 *
 * @param <T> The type of content items in the page
 */
@Schema(description = "Generic paginated response containing a list of items and pagination metadata")
public record PageResponse<T>(
  @Schema(description = "List of items in the current page", example = "[...]")
  List<T> content,

  @Schema(description = "Current page number (0-indexed)", example = "0")
  int page,

  @Schema(description = "Number of items per page", example = "10")
  int size,

  @Schema(description = "Total number of items across all pages", example = "156")
  long totalElements,

  @Schema(description = "Total number of pages available", example = "16")
  int totalPages,

  @Schema(description = "Whether this is the first page", example = "true")
  boolean first,

  @Schema(description = "Whether this is the last page", example = "false")
  boolean last
) {}
