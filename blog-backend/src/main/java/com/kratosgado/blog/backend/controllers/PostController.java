package com.kratosgado.blog.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.models.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management APIs")
public class PostController {

  private final PostService postService;

  @PostMapping
  @Operation(summary = "Create a new post", description = "Creates a new blog post with the provided details. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredCreateEndpoint
  public ResponseEntity<ResponseDto<PostResponse>> createPost(
      @Valid @RequestBody @Parameter(description = "Post creation request") CreatePostRequest request,
      @AuthenticationPrincipal User user) {
    var post = postService.createPost(request, user);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ResponseDto.success("Post created successfully", post));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a post", description = "Updates an existing blog post. Only the post author can update it.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredUpdateEndpoint
  public PostResponse updatePost(
      @PathVariable @Parameter(description = "Post ID") Long id,
      @Valid @RequestBody @Parameter(description = "Post update request") UpdatePostRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    return postService.updatePost(id, request, userId);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a post", description = "Deletes a blog post by ID. Only the post author can delete it.", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteEndpoint
  public ResponseDto<Void> deletePost(
      @PathVariable @Parameter(description = "Post ID") Long id) {
    Long userId = SecurityUtils.getCurrentUserId();
    postService.deletePost(id, userId);
    return ResponseDto.success("Post deleted successfully", null);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a post by ID", description = "Retrieves a single blog post by its ID. Public access.")
  @GetEnpoint
  public PostResponse getPost(
      @PathVariable @Parameter(description = "Post ID") Long id) {
    return postService.getPostById(id);
  }

  @GetMapping
  @Operation(summary = "Get all published posts", description = "Retrieves a paginated list of published blog posts with sorting options. Public access.")
  @GetEnpoint
  public PageResponse<PostResponse> getPosts(
      @RequestParam(defaultValue = "1") @Parameter(description = "Page number (1-indexed)") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Page size") int size) {
    return postService.getPublishedPosts(page, size);
  }

  @GetMapping("/search")
  @Operation(summary = "Search posts", description = "Searches for posts by keyword in title and content")
  @GetEnpoint
  public PageResponse<PostResponse> searchPosts(
      @RequestParam @Parameter(description = "Search keyword") String keyword,
      @RequestParam(defaultValue = "1") @Parameter(description = "Page number (1-indexed)") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Page size") int size) {
    return postService.searchPosts(keyword, page, size);

  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get posts by user", description = "Retrieves all posts created by a specific user")
  @GetEnpoint
  public PageResponse<PostResponse> getUserPosts(
      @PathVariable @Parameter(description = "User ID") Long userId,
      @RequestParam(defaultValue = "1") @Parameter(description = "Page number (1-indexed)") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Page size") int size) {
    return postService.getUserPosts(userId, page, size);

  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get posts by category", description = "Retrieves all posts in a specific category")
  @GetEnpoint
  public PageResponse<PostResponse> getCategoryPosts(
      @PathVariable @Parameter(description = "Category ID") Long categoryId,
      @RequestParam(defaultValue = "1") @Parameter(description = "Page number (1-indexed)") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Page size") int size) {
    return postService.getPostsByCategory(categoryId, page, size);

  }
}
