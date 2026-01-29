
package com.kratosgado.blog.backend.utils;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.models.Post;

public class DtoMapper {

  public static PostDetails toPostResponse(Post post) {
    return (PostDetails) post;
  }

  public static <T> PageResponse<T> toPageResponse(Page<T> page, Pageable pageable) {
    return new PageResponse<>(page.getContent(), pageable.getPageNumber() + 1, page.getNumber(),
        page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
  }

  public static <T> PageResponse<T> toPageResponse(List<T> content, int page, int size, int totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / size);
    boolean isFirst = page == 0;
    boolean isLast = page >= totalPages - 1;
    return new PageResponse<>(content, page, size, totalElements, totalPages, isFirst, isLast);
  }

}
