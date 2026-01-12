package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import com.kratosgado.blog.dtos.request.CreatePostDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Post {
  private int id;
  private int userId;
  private Integer categoryId;  // Must have a category
  private String title;
  private String content;
  private String excerpt;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private int views;
  private int likesCount;
  private String coverImage;
  private String authorName;
  private String authorAvatarUrl;

  public Post(CreatePostDto dto) {
    userId = dto.userId();
    categoryId = dto.categoryId();
    title = dto.title();
    content = dto.content();
    excerpt = dto.excerpt();
    status = dto.status();
    coverImage = dto.coverImage();
  }
}
