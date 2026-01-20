package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.kratosgado.blog.enums.CommentStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "comments")
public class Comment {
  @Id
  private String id;

  @Field("post_id")
  private Long postId;

  @Field("user_id")
  private Long userId;

  private String content;

  private CommentStatus status;

  @Field("created_at")
  private LocalDateTime createdAt;

  @Field("updated_at")
  private LocalDateTime updatedAt;

  @Transient
  private String authorName;

  @Transient
  private String authorAvatarUrl;

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
