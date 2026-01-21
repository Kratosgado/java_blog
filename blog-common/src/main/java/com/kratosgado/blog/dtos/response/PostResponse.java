package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

import com.kratosgado.blog.enums.PostStatus;

public record PostResponse(
    Long id,
    AuthorSummary author,
    CategorySummary category,
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
}
