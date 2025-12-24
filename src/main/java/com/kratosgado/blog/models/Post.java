package com.kratosgado.blog.models;

import java.time.LocalDateTime;

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

  public Post(int userId, String title, String content, String excerpt, String status) {
    this.userId = userId;
    this.title = title;
    this.content = content;
    this.excerpt = excerpt;
    this.status = status;
  }
}
