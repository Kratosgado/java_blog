package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Request DTO for paginated search queries. Extends PageRequest to add keyword search
 * functionality. Used for full-text search across blog posts with pagination and sorting support.
 */
@Schema(description = "Request parameters for paginated search with keyword filtering")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SearchPageRequest extends PageRequest {
  @Schema(description = "Search keyword to filter results", example = "java spring boot")
  private String keyword;
}
