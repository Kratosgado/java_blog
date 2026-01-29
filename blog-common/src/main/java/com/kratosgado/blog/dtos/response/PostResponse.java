package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;

import com.kratosgado.blog.enums.PostStatus;

public interface PostResponse {

  public interface IPost {

    Long getId();

    String getTitle();

    String getSlug();

    String getContent();

    String getExcerpt();

    PostStatus getStatus();

    String getCoverImage();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Integer getViews();

    Integer getLikesCount();
  }

  public interface WithUser {
    AuthorSummary getUser();
  }

  public interface WithCategory {
    CategorySummary getCategory();
  }

  public interface WithTag {
    TagSummary getTags();
  }

  public interface PostDetails extends IPost, WithUser, WithCategory, WithTag {
  }

  public interface PostWithoutUser extends IPost, WithCategory, WithTag {
  }

  public interface PostWithoutTag extends IPost, WithUser, WithCategory {
  }

  public interface PostWithoutCategory extends IPost, WithUser, WithTag {
  }

}
