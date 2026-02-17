package com.kratosgado.blog.models;

import com.kratosgado.blog.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {
  private Long id;

  private String title;

  private String slug;

  private String content;

  private String excerpt;

  private PostStatus status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @Builder.Default
  private Integer views = 0;

  @Builder.Default
  private Integer likesCount = 0;

  private String coverImage;

  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) {
      status = PostStatus.draft;
    }
  }

  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  private User user;

  private Long userId;

  private Category category;

  private List<Tag> tags;
}
