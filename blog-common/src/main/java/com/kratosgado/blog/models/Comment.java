package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.kratosgado.blog.enums.CommentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

  private String authorName;
  private String authorAvatarUrl;

  @Field("created_at")
  private LocalDateTime createdAt;

  @Field("updated_at")
  private LocalDateTime updatedAt;

  public void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) {
      status = CommentStatus.pending;
    }
  }

  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
