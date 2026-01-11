package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import com.kratosgado.blog.utils.enums.CommentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Comment {
  private int id;
  private int postId;
  private int userId;
  private String content;
  private CommentStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String authorName;
  private String authorAvatarUrl;

  public Comment(int postId, int userId, String content) {
    this.postId = postId;
    this.userId = userId;
    this.content = content;
    this.status = CommentStatus.PENDING;
  }
}
