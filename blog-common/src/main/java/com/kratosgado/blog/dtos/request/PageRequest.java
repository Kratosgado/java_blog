package com.kratosgado.blog.dtos.request;

public record PageRequest(
    int page,
    int size,
    String sortBy,
    String sortDir) {
}
