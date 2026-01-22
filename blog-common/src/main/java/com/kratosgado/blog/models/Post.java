package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
  private Integer id;
  private Long userId;
  private Integer categoryId;
  private String title;
  private String slug;
  private String content;
  private String excerpt;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Integer views = 0;
  private Integer likesCount = 0;
  private String coverImage;

  // These fields are populated from joins
  private String authorName;
  private String authorAvatarUrl;
  private String categoryName;
}
