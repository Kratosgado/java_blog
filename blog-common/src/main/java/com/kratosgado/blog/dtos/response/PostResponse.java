package com.kratosgado.blog.dtos.response;

import com.kratosgado.blog.models.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
  Long id,
  String title,
  String slug,
  String excerpt,
  String content,
  String coverImage,
  PostStatus status,
  Long authorId,
  String authorName,
  String authorAvatarUrl,
  Long categoryId,
  String categoryName,
  List<String> tags,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
