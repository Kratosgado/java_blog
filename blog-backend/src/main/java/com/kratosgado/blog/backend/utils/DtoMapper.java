
package com.kratosgado.blog.backend.utils;

import com.kratosgado.blog.dtos.response.AuthorSummary;
import com.kratosgado.blog.dtos.response.CategorySummary;
import com.kratosgado.blog.dtos.response.CommentResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.dtos.response.ReviewResponse;
import com.kratosgado.blog.dtos.response.TagSummary;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

public class DtoMapper {

  public static <T> T map(Object source, Class<T> targetClass) {
    return targetClass.cast(source);
  }

  public static PostResponse toPostResponse(Post post) {
    return new PostResponse(
        post.getId(),
        toAuthorSummary(post.getUser()),
        toCategorySummary(post.getCategory()),
        post.getTitle(),
        post.getContent(),
        post.getExcerpt(),
        post.getStatus(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getViews(),
        post.getLikesCount(),
        post.getCoverImage(),
        post.getTags().stream().map(DtoMapper::toTagSummary).toList());

  }

  public static AuthorSummary toAuthorSummary(User user) {
    if (user == null) {
      return null;
    }
    return new AuthorSummary(user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl());
  }

  public static TagSummary toTagSummary(Tag tag) {
    if (tag == null) {
      return null;
    }
    return new TagSummary(tag.getId(), tag.getName(), tag.getSlug());
  }

  public static CategorySummary toCategorySummary(Category category) {
    if (category == null) {
      return null;
    }
    return new CategorySummary(category.getId(), category.getName(), category.getSlug());
  }

  public static CommentResponse toCommentResponse(Comment comment) {
    if (comment == null) {
      return null;
    }
    return new CommentResponse(
        comment.getId(),
        comment.getPostId(),
        new AuthorSummary(
            comment.getUserId(),
            comment.getAuthorName(),
            null, // email not stored in snapshot
            comment.getAuthorAvatarUrl()),
        comment.getContent(),
        comment.getStatus().name(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }

  public static ReviewResponse toReviewResponse(Review review) {
    if (review == null) {
      return null;
    }
    return new ReviewResponse(
        review.getId(),
        review.getPostId(),
        new AuthorSummary(
            review.getUserId(),
            review.getAuthorName(),
            null, // email not stored in snapshot
            review.getAuthorAvatarUrl()),
        review.getRating(),
        review.getTitle(),
        review.getContent(),
        review.getCreatedAt(),
        review.getUpdatedAt(),
        review.isHelpful());
  }
}
