
package com.kratosgado.blog.backend.utils;

import java.util.List;

import com.kratosgado.blog.dtos.response.AuthorSummary;
import com.kratosgado.blog.dtos.response.CategorySummary;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.dtos.response.TagSummary;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

public class DtoMapper {

  public static PostResponse toPostResponse(Post post) {
    return new PostResponse(
        post.getId(),
        toAuthorSummary(post.getUser()),
        toCategorySummary(post.getCategory()),
        post.getSlug(),
        post.getTitle(),
        post.getContent(),
        post.getExcerpt(),
        post.getStatus(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getViews(),
        post.getLikesCount(),
        post.getCoverImage(),
        post.getTags() != null ? post.getTags().stream().map(DtoMapper::toTagSummary).toList() : null);

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

  public static <T> PageResponse<T> toPageResponse(List<T> content, int page, int size, int totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / size);
    boolean isFirst = page == 1;
    boolean isLast = page >= totalPages;
    return new PageResponse<>(content, page, size, totalElements, totalPages, isFirst, isLast);
  }

}
