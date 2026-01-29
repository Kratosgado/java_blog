package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import java.util.List;

import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.interfaces.HasId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Post implements HasId {
  private Long id;

  private String title;

  private String slug;

  private String content;

  private String excerpt;

  private PostStatus status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private Integer views = 0;

  private Integer likesCount = 0;

  private String coverImage;

  public Post() {
    onCreate();
  }

  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    status = PostStatus.draft;
  }

  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  private User user;

  private Long userId;

  private Category category;

  private Long categoryId;

  private List<Tag> tags;
}
