package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import com.kratosgado.blog.dtos.request.CreatePostDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
  private int id;
  private int userId;
  private String title;
  private String content;
  private String excerpt;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private int views;
  private String featuredImage;

  public Post(CreatePostDto dto) {
    userId = dto.userId();
    title = dto.title();
    content = dto.content();
    excerpt = dto.excerpt();
    status = dto.status();
    featuredImage = dto.featuredImage();
  }
}
