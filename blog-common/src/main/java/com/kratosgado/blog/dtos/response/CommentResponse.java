
package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;

import com.kratosgado.blog.enums.CommentStatus;

public class CommentResponse {
  public record CommentWithoutUser(String id, Long postId, String content, CommentStatus status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
  }

  public record CommentWithoutPostId(String id, Long userId, String content, CommentStatus status,
      String authorName, String authorAvatarUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
  }

}
