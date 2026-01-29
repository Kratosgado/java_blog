package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
@Document(collection = "reviews")
public class Review {
  @Id
  private String id;

  @Field("post_id")
  private Long postId;

  @Field("user_id")
  private Long userId;

  private int rating; // 1-5 stars

  private String title;

  private String content;

  private String authorName;
  private String authorAvatarUrl;

  @Field("created_at")
  private LocalDateTime createdAt;

  @Field("updated_at")
  private LocalDateTime updatedAt;

  private boolean helpful = false;

  public void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
