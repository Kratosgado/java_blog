package com.kratosgado.blog.backend.controllers.v2;

import com.kratosgado.blog.backend.annotations.OpenApi.GetEndpoint;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts V2", description = "Post management APIs V2 (Optimized)")
@RequiredArgsConstructor
public class PostControllerV2 {

  private final PostService postService;

  @GetMapping("/search")
  @Operation(
      summary = "Search posts (Optimized)",
      description =
          "Searches for posts by keyword in title and content using full-text search vector")
  @GetEndpoint
  public PageResponse<PostView> searchPosts(@ParameterObject SearchPageRequest request) {
    return postService.searchPosts(request);
  }

  @GetMapping("/trending")
  @Operation(
      summary = "Get trending posts",
      description = "Retrieves trending posts based on views and recent activity")
  @GetEndpoint
  public PageResponse<PostView> getTrendingPosts(@ParameterObject PageRequest page) {
    return postService.getTrendingPosts(page);
  }

  @GetMapping("/category/{categoryId}/optimized")
  @Operation(
      summary = "Get posts by category (Optimized)",
      description = "Retrieves published posts by category using optimized query")
  @GetEndpoint
  public PageResponse<PostView> getCategoryPostsOptimized(
      @PathVariable @Parameter(description = "Category ID") Long categoryId,
      @ParameterObject PageRequest page) {
    return postService.getPublishedPostsByCategoryOptimized(categoryId, page);
  }

  @GetMapping("/tag/{tagId}/optimized")
  @Operation(
      summary = "Get posts by tag (Optimized)",
      description = "Retrieves published posts by tag using optimized query")
  @GetEndpoint
  public PageResponse<PostView> getTagPostsOptimized(
      @PathVariable @Parameter(description = "Tag ID") Long tagId,
      @ParameterObject PageRequest page) {
    return postService.getPublishedPostsByTagOptimized(tagId, page);
  }
}
