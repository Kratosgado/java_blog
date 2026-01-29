package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;

public interface PostWithoutUser {
  Long getId();

  String getSlug();

  String getTitle();

  String getContent();

  String getExcerpt();

  String getCoverImage();

  String getStatus();

  LocalDateTime getCreatedAt();

  LocalDateTime getUpdatedAt();

  Integer getViews();

  Integer getLikesCount();

}
