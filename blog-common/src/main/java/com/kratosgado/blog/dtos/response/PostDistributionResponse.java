package com.kratosgado.blog.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for post status distribution statistics.
 * Provides counts of posts grouped by their publication status.
 * Used in analytics dashboards to visualize content distribution.
 */
@Schema(description = "Post distribution by status (published, draft, private)")
public record PostDistributionResponse(
    @Schema(description = "Number of published posts visible to the public", example = "156")
    long published,

    @Schema(description = "Number of draft posts not yet published", example = "23")
    long draft,

    @Schema(description = "Number of private posts visible only to author", example = "8")
    long privateCount) {
}
