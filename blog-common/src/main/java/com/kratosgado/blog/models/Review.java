package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {
  @Id
  private String id;

  @Field("post_id")
  private Long postId;

  @Field("user_id")
  private Long userId;

  @Field("author_name")
  private String authorName;

  @Field("author_avatar_url")
  private String authorAvatarUrl;

  private int rating; // 1-5 stars

  private String title;

  private String content;

  @Field("created_at")
  private LocalDateTime createdAt;

  @Field("updated_at")
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
