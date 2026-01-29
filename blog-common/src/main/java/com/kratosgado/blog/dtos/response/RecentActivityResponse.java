package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

public record RecentActivityResponse(
    List<RecentPost> latestPosts,
    List<RecentComment> latestComments) {

  public record RecentPost(
      Long id,
      String title,
      String slug,
      String authorName,
      LocalDateTime createdAt) {
  }

  public record RecentComment(
      String id,
      Long postId,
      String authorName,
      String content,
      LocalDateTime createdAt) {
  }
}
