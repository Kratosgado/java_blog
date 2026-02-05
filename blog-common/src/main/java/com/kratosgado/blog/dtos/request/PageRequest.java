package com.kratosgado.blog.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Base request DTO for pagination and sorting. Provides common pagination parameters that can be
 * converted to Spring Data Pageable. Used as a base class for all paginated API requests.
 */
@Schema(description = "Request parameters for pagination and sorting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PageRequest {
  @Schema(
      description = "Page number (zero-based)",
      example = "0",
      defaultValue = "0",
      minimum = "0")
  @Builder.Default
  protected int page = 0;

  @Schema(
      description = "Number of items per page",
      example = "10",
      defaultValue = "10",
      minimum = "1",
      maximum = "100")
  @Builder.Default
  protected int size = 10;

  @Schema(description = "Field name to sort by", example = "createdAt", defaultValue = "id")
  @Builder.Default
  protected String sortBy = "id";

  @Schema(
      description = "Sort direction",
      example = "desc",
      defaultValue = "desc",
      allowableValues = {"asc", "desc"})
  @Builder.Default
  protected String sortDir = "desc";

  public int getOffset() {
    return page * size;
  }

  public Pageable toPageable() {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    return org.springframework.data.domain.PageRequest.of(page, size, sort);
  }
}
