package com.kratosgado.blog.dtos.response;

public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    Long postCount) {
}
