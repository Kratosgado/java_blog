package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import com.kratosgado.blog.enums.CommentStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Comment {
  private String id;

  private Long postId;

  private Long userId;

  private String authorName;

  private String authorAvatarUrl;

  private String content;

  private CommentStatus status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public Comment(Long postId, Long userId, String content) {
    this.postId = postId;
    this.userId = userId;
    this.content = content;
    this.status = CommentStatus.pending;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void setUpdatedAt() {
    this.updatedAt = LocalDateTime.now();
  }
}
