package com.kratosgado.blog.dtos.response;

import java.util.List;

public record EngagementStatsResponse(
    List<PostEngagementSummary> topPostsByViews,
    List<PostEngagementSummary> topPostsByLikes,
    List<CategorySummaryWithCount> popularCategories) {

  public record PostEngagementSummary(
      Long id,
      String title,
      String slug,
      int views,
      int likesCount) {
  }

  public record CategorySummaryWithCount(
      Long id,
      String name,
      String slug,
      int postCount) {
  }
}
