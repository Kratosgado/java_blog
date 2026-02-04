
package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;
import com.kratosgado.blog.enums.CommentStatus;

public interface CommentResponse {
  public interface CommentSummary {

    String getId();

    String getContent();

    CommentStatus getStatus();

    LocalDateTime getCreatedAt();
  }

  interface WithPostId {
    Long getPostId();
  }

  interface WithUser {
    String getAuthorName();

    String getAuthorAvatarUrl();
  }

  public interface CommentWithoutUser extends CommentSummary, WithPostId {
  }

  public interface CommentWithoutPostId extends CommentSummary, WithUser {
  }

}