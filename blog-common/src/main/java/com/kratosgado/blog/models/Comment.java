package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
  @Indexed
  private Long postId;

  @Field("user_id")
  @Indexed
  private Long userId;

  @Field("author_name")
  private String authorName;

  @Field("author_avatar_url")
  private String authorAvatarUrl;

  private String content;

  @Indexed
  private CommentStatus status;

  @Field("created_at")
  @Indexed
  private LocalDateTime createdAt;

  @Field("updated_at")
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
