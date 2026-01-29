package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
  private String id;

  private Long postId;

  private Long userId;

  private String authorName;

  private String authorAvatarUrl;

  private int rating; // 1-5 stars

  private String title;

  private String content;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private boolean helpful = false;

  public Review(Long postId, Long userId, int rating, String title, String content) {
    this.postId = postId;
    this.userId = userId;
    this.rating = rating;
    this.title = title;
    this.content = content;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void setUpdatedAt() {
    this.updatedAt = LocalDateTime.now();
  }
}
