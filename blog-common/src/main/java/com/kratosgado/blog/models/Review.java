package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
  private int id;
  private int postId;
  private int userId;
  private int rating; // 1-5 stars
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String authorName;
  private String authorAvatarUrl;
  private boolean helpful;

  public Review(int postId, int userId, int rating, String title, String content) {
    this.postId = postId;
    this.userId = userId;
    this.rating = rating;
    this.title = title;
    this.content = content;
  }
}
