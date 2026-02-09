package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

  public Pageable toPageable(Pageable pageable) {
    Sort rankSort =
        Sort.by(
            Sort.Direction.DESC,
            "ts_rank(search_vector, websearch_to_tsquery('english', '" + keyword + "'))");
    return org.springframework.data.domain.PageRequest.of(page, size, rankSort);
  }
}
