
package com.kratosgado.blog.backend.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

  public static <T> PageResponse<T> toPageResponse(Page<T> page, Pageable pageable) {
    return new PageResponse<>(page.getContent(), pageable.getPageNumber() + 1, page.getNumber(),
        page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
  }

}
