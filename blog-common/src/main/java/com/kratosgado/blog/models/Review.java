package com.kratosgado.blog.models;

import java.time.LocalDateTime;

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
public class Review {
  private String id;

  private Long postId;

  private Long userId;

  private int rating; // 1-5 stars

  private String title;

  private String content;

  private String authorName;
  private String authorAvatarUrl;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @Builder.Default
  private boolean helpful = false;

  public void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
