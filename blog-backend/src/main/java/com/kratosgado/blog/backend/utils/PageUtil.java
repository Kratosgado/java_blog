package com.kratosgado.blog.backend.utils;

import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {
  public static Pageable toPageable(PageRequest request) {
    Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
    return org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), sort);
  }

  public static Pageable toPageable(SearchPageRequest request) {
    if (request.getKeyword() == null || request.getKeyword().isBlank()) {
        return toPageable((PageRequest) request);
    }
    Sort rankSort =
        Sort.by(
            Sort.Direction.DESC,
            "ts_rank(search_vector, websearch_to_tsquery('english', '" + request.getKeyword() + "'))");
    return org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), rankSort);
  }
}
