package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
  private int id;
  private int postId;
  private int userId;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String authorName;

  public Comment(int postId, int userId, String content) {
    this.postId = postId;
    this.userId = userId;
    this.content = content;
  }
}
