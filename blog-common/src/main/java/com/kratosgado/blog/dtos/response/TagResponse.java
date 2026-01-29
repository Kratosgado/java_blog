package com.kratosgado.blog.dtos.response;

public record TagResponse(
    Long id,
    String name,
    String slug,
    String description,
    Integer postCount) {
}
