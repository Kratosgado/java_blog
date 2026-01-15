package com.kratosgado.blog.backend.graphql;

import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.backend.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PostGraphQLController {

  private final PostService postService;

  @QueryMapping
  public Post post(@Argument Long id) {
    return postService.getPostById(id);
  }

  @QueryMapping
  public Map<String, Object> posts(
      @Argument(name = "page") int page,
      @Argument(name = "size") int size,
      @Argument(name = "sortBy") String sortBy,
      @Argument(name = "sortDir") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    PageRequest pageRequest = PageRequest.of(page, size, sort);
    Page<Post> postsPage = postService.getPublishedPosts(pageRequest);

    Map<String, Object> result = new HashMap<>();
    result.put("content", postsPage.getContent());
    result.put("totalPages", postsPage.getTotalPages());
    result.put("totalElements", postsPage.getTotalElements());
    result.put("currentPage", postsPage.getNumber());

    return result;
  }

  @QueryMapping
  public Map<String, Object> searchPosts(
      @Argument String keyword,
      @Argument int page,
      @Argument int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> postsPage = postService.searchPosts(keyword, pageRequest);

    Map<String, Object> result = new HashMap<>();
    result.put("content", postsPage.getContent());
    result.put("totalPages", postsPage.getTotalPages());
    result.put("totalElements", postsPage.getTotalElements());
    result.put("currentPage", postsPage.getNumber());

    return result;
  }

  @QueryMapping
  public Map<String, Object> postsByCategory(
      @Argument Long categoryId,
      @Argument int page,
      @Argument int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> postsPage = postService.getPostsByCategory(categoryId, pageRequest);

    Map<String, Object> result = new HashMap<>();
    result.put("content", postsPage.getContent());
    result.put("totalPages", postsPage.getTotalPages());
    result.put("totalElements", postsPage.getTotalElements());
    result.put("currentPage", postsPage.getNumber());

    return result;
  }

  @QueryMapping
  public Map<String, Object> postsByUser(
      @Argument Long userId,
      @Argument int page,
      @Argument int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> postsPage = postService.getUserPosts(userId, pageRequest);

    Map<String, Object> result = new HashMap<>();
    result.put("content", postsPage.getContent());
    result.put("totalPages", postsPage.getTotalPages());
    result.put("totalElements", postsPage.getTotalElements());
    result.put("currentPage", postsPage.getNumber());

    return result;
  }

}
