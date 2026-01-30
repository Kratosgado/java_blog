package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

import com.kratosgado.blog.enums.PostStatus;

public interface PostResponse {

  public interface PostSummary {
    Long getId();

    String getTitle();

    String getSlug();

    String getExcerpt();

    PostStatus getStatus();

    String getCoverImage();

    LocalDateTime getCreatedAt();

    Integer getViews();

    Integer getLikesCount();
  }

  public interface IPost extends PostSummary {
    String getContent();

    LocalDateTime getUpdatedAt();
  }

  public interface WithUser {
    AuthorSummary getUser();
  }

  public interface WithCategory {
    CategorySummary getCategory();
  }

  public interface WithTag {
    List<? extends TagSummary> getTags();
  }

  public interface PostView extends PostSummary, WithUser, WithCategory, WithTag {
  }

  public interface PostDetails extends IPost, WithUser, WithCategory, WithTag {
  }

  public interface PostWithoutUser extends PostSummary, WithCategory, WithTag {
  }

  public interface PostWithoutTag extends PostSummary, WithUser, WithCategory {
  }

  public interface PostWithoutCategory extends PostSummary, WithUser, WithTag {
  }

}
