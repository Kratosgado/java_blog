
package com.kratosgado.blog.backend.utils;

import java.util.List;

import com.kratosgado.blog.dtos.response.AuthorSummary;
import com.kratosgado.blog.dtos.response.CategorySummary;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.dtos.response.TagSummary;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

public class DtoMapper {

  public static <T> T map(Object source, Class<T> targetClass) {
    return targetClass.cast(source);
  }

  /**
   * Convert Post model to PostResponse DTO
   * Note: This now requires tags to be passed in since Post no longer has entity
   * relationships
   */
  public static PostResponse toPostResponse(Post post, User author, Category category, List<Tag> tags) {
    return new PostResponse(
        post.getId().longValue(),
        toAuthorSummary(author),
        toCategorySummary(category),
        post.getSlug(),
        post.getTitle(),
        post.getContent(),
        post.getExcerpt(),
        PostStatus.valueOf(post.getStatus()),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getViews(),
        post.getLikesCount(),
        post.getCoverImage(),
        tags != null ? tags.stream().map(DtoMapper::toTagSummary).toList() : List.of());
  }

  /**
   * Simplified version when you already have user info in the Post (from JOIN)
   */
  public static PostResponse toPostResponse(Post post, List<Tag> tags) {
    // Create AuthorSummary from fields that may be populated by JOIN
    AuthorSummary author = null;
    if (post.getAuthorName() != null) {
      author = new AuthorSummary(
          post.getUserId().longValue(),
          post.getAuthorName(),
          null, // email not included in simple joins
          post.getAuthorAvatarUrl());
    }

    // Create CategorySummary from fields that may be populated by JOIN
    CategorySummary category = null;
    if (post.getCategoryName() != null && post.getCategoryId() != null) {
      category = new CategorySummary(
          post.getCategoryId().longValue(),
          post.getCategoryName(),
          null); // slug not included in simple joins
    }

    return new PostResponse(
        post.getId().longValue(),
        author,
        category,
        post.getSlug(),
        post.getTitle(),
        post.getContent(),
        post.getExcerpt(),
        PostStatus.valueOf(post.getStatus()),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getViews(),
        post.getLikesCount(),
        post.getCoverImage(),
        tags != null ? tags.stream().map(DtoMapper::toTagSummary).toList() : List.of());
  }

  public static AuthorSummary toAuthorSummary(User user) {
    if (user == null) {
      return null;
    }
    return new AuthorSummary(user.getId().longValue(), user.getUsername(), user.getEmail(), user.getAvatarUrl());
  }

  public static TagSummary toTagSummary(Tag tag) {
    if (tag == null) {
      return null;
    }
    return new TagSummary(tag.getId().longValue(), tag.getName(), tag.getSlug());
  }

  public static CategorySummary toCategorySummary(Category category) {
    if (category == null) {
      return null;
    }
    return new CategorySummary(category.getId().longValue(), category.getName(), category.getSlug());
  }

  public static <T> PageResponse<T> toPageResponse(List<T> content, int page, int size, long totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / size);
    boolean isFirst = page == 1;
    boolean isLast = page >= totalPages;

    return new PageResponse<>(content, page, page - 1, totalElements, totalPages, isFirst, isLast);
  }

}
