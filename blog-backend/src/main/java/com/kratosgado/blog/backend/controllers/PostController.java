package com.kratosgado.blog.backend.controllers;

import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management APIs")
public class PostController {

  private static final Logger logger = LoggerFactory.getLogger(PostController.class);

  private final PostService postService;

  @PostMapping
  @Operation(summary = "Create a new post", description = "Creates a new blog post with the provided details. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Post created successfully", content = @Content(schema = @Schema(implementation = Post.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<ResponseDto<Post>> createPost(
      @Valid @RequestBody @Parameter(description = "Post creation request") CreatePostRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Post post = postService.createPost(request, userId);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ResponseDto.success("Post created successfully", post));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a post", description = "Updates an existing blog post. Only the post author can update it.", security = @SecurityRequirement(name = "bearer-jwt"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - not post author")
  })
  public ResponseEntity<ResponseDto<Post>> updatePost(
      @PathVariable @Parameter(description = "Post ID") Long id,
      @Valid @RequestBody @Parameter(description = "Post update request") UpdatePostRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Post post = postService.updatePost(id, request, userId);
    return ResponseEntity.ok(ResponseDto.success("Post updated successfully", post));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a post", description = "Deletes a blog post by ID. Only the post author can delete it.", security = @SecurityRequirement(name = "bearer-jwt"))
  public ResponseEntity<ResponseDto<Void>> deletePost(
      @PathVariable @Parameter(description = "Post ID") Long id) {
    Long userId = SecurityUtils.getCurrentUserId();
    postService.deletePost(id, userId);
    return ResponseEntity.ok(ResponseDto.success("Post deleted successfully", null));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a post by ID", description = "Retrieves a single blog post by its ID. Public access.")
  public ResponseEntity<ResponseDto<Post>> getPost(
      @PathVariable @Parameter(description = "Post ID") Long id) {
    Post post = postService.getPostById(id);
    return ResponseEntity.ok(ResponseDto.success(post));
  }

  @GetMapping
  @Operation(summary = "Get all published posts", description = "Retrieves a paginated list of published blog posts with sorting options. Public access.")
  public ResponseEntity<ResponseDto<Map<String, Object>>> getPosts(
      @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Page size") int size,
      @RequestParam(defaultValue = "createdAt") @Parameter(description = "Sort field") String sortBy,
      @RequestParam(defaultValue = "DESC") @Parameter(description = "Sort direction (ASC/DESC)") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("ASC")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    PageRequest pageRequest = PageRequest.of(page, size, sort);
    Page<Post> posts = postService.getPublishedPosts(pageRequest);

    Map<String, Object> response = Map.of(
        "content", posts.getContent(),
        "totalPages", posts.getTotalPages(),
        "totalElements", posts.getTotalElements(),
        "currentPage", posts.getNumber());

    return ResponseEntity.ok(ResponseDto.success(response));
  }

  @GetMapping("/search")
  @Operation(summary = "Search posts", description = "Searches for posts by keyword in title and content")
  public ResponseEntity<ResponseDto<Map<String, Object>>> searchPosts(
      @RequestParam @Parameter(description = "Search keyword") String keyword,
      @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Page size") int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> posts = postService.searchPosts(keyword, pageRequest);

    Map<String, Object> response = Map.of(
        "content", posts.getContent(),
        "totalPages", posts.getTotalPages(),
        "totalElements", posts.getTotalElements(),
        "currentPage", posts.getNumber());

    return ResponseEntity.ok(ResponseDto.success(response));
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get posts by user", description = "Retrieves all posts created by a specific user")
  public ResponseEntity<ResponseDto<Map<String, Object>>> getUserPosts(
      @PathVariable @Parameter(description = "User ID") Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> posts = postService.getUserPosts(userId, pageRequest);

    Map<String, Object> response = Map.of(
        "content", posts.getContent(),
        "totalPages", posts.getTotalPages(),
        "totalElements", posts.getTotalElements(),
        "currentPage", posts.getNumber());

    return ResponseEntity.ok(ResponseDto.success(response));
  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get posts by category", description = "Retrieves all posts in a specific category")
  public ResponseEntity<ResponseDto<Map<String, Object>>> getCategoryPosts(
      @PathVariable @Parameter(description = "Category ID") Long categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> posts = postService.getPostsByCategory(categoryId, pageRequest);

    Map<String, Object> response = Map.of(
        "content", posts.getContent(),
        "totalPages", posts.getTotalPages(),
        "totalElements", posts.getTotalElements(),
        "currentPage", posts.getNumber());

    return ResponseEntity.ok(ResponseDto.success(response));
  }
}
