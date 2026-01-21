package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;

public record CommentResponse(
    String id,
    Long postId,
    AuthorSummary author,
    String content,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
