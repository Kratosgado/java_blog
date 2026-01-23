package com.kratosgado.blog.dtos.response;

public record AuthorSummary(
    Long id,
    String username,
    String email,
    String avatarUrl) {
}
