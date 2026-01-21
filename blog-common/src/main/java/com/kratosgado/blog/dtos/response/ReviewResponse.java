package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;

public record ReviewResponse(
    String id,
    Long postId,
    AuthorSummary author,
    int rating,
    String title,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean helpful) {
}
