package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
    Long id,
    AuthorSummary author,
    CategorySummary category,
    String title,
    String content,
    String excerpt,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Integer views,
    Integer likesCount,
    String coverImage,
    List<TagSummary> tags) {
}
