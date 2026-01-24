package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

import com.kratosgado.blog.enums.PostStatus;

public record PostResponse(
    Long id,
    AuthorSummary author,
    CategorySummary category,
    String slug,
    String title,
    String content,
    String excerpt,
    PostStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Integer views,
    Integer likesCount,
    String coverImage,
    List<TagSummary> tags) {

  public Long authorId() {
    return author != null ? author.id() : null;
  }

  public Long categoryId() {
    return category != null ? category.id() : null;
  }

  public String authorName() {
    return author != null ? author.username() : null;
  }

  public String authorAvatarUrl() {
    return author != null ? author.avatarUrl() : null;
  }
}
