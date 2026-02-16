package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutUser;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Post management APIs")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping
  @SecuredCreateEndpoint(
      summary = "Create a new post (Secured)",
      description = "Creates a new blog post with the provided details. Requires authentication.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
  public ResponseDto<Post> createPost(
      @Valid @RequestBody @Parameter(description = "Post creation request")
          CreatePostRequest request,
      @AuthenticationPrincipal User user) {
    Post post = postService.createPost(request, user);
    return ResponseDto.success(HttpStatus.CREATED.value(), "Post created successfully", post);
  }

  @PutMapping("/{id}")
  @SecuredUpdateEndpoint(
      summary = "Update a post",
      description = "Updates an existing blog post. Only the post author can update it.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
  public ResponseDto<Post> updatePost(
      @PathVariable @Parameter(description = "Post ID") Long id,
      @Valid @RequestBody @Parameter(description = "Post update request")
          UpdatePostRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Post post = postService.updatePost(id, request, userId);
    return ResponseDto.success("Post updated successfully", post);
  }

  @PutMapping("/{id}/publish")
  @SecuredUpdateEndpoint(
      summary = "Publish a post",
      description = "Updates a blog post status to published. Only the post author can publish it.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
  public ResponseDto<Post> publishPost(@PathVariable @Parameter(description = "Post ID") Long id) {
    Long userId = SecurityUtils.getCurrentUserId();
    Post post = postService.publishPost(id, userId);
    return ResponseDto.success("Post published successfully", post);
  }

  @DeleteMapping("/{id}")
  @DeleteEndpoint(
      summary = "Delete a post",
      description = "Deletes a blog post by ID. Only the post author can delete it.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
  public ResponseDto<Void> deletePost(
      @PathVariable @Parameter(description = "Post ID") Long id,
      @AuthenticationPrincipal User user) {
    postService.deletePost(id, user.getId());
    return ResponseDto.success("Post deleted successfully", null);
  }

  @GetMapping("/{id}")
  @GetEndpoint(
      summary = "Get a post by ID",
      description = "Retrieves a single blog post by its ID. Public access.")
  public ResponseDto<PostDetails> getPost(@PathVariable @Parameter(description = "Post ID") Long id) {
    PostDetails post = postService.getPostById(id);
    return ResponseDto.success(post);
  }

  @GetMapping("/slug/{slug}")
  @GetEndpoint(
      summary = "Get a post by slug",
      description =
          "Retrieves a single blog post by its slug. Uses cache for better performance. Public"
              + " access.")
  public ResponseDto<PostDetails> getPostBySlug(
      @PathVariable @Parameter(description = "Post slug") String slug) {
    PostDetails post = postService.getPostBySlug(slug);
    return ResponseDto.success(post);
  }

  @GetMapping()
  @GetEndpoint(
      summary = "Get all published posts",
      description =
          "Retrieves a paginated list of published blog posts with sorting options. Public access.")
  public ResponseDto<PageResponse<PostView>> getPosts(@ParameterObject PageRequest page) {
    PageResponse<PostView> posts = postService.getPublishedPosts(page);
    return ResponseDto.success(posts);
  }

  @GetMapping("/search")
  @GetEndpoint(
      summary = "Search posts",
      description = "Searches for posts by keyword in title and content")
  public ResponseDto<PageResponse<PostView>> searchPosts(@ParameterObject SearchPageRequest request) {
    PageResponse<PostView> posts = postService.searchPostsV1(request);
    return ResponseDto.success(posts);
  }

  @GetMapping("/user/{userId}")
  @GetEndpoint(
      summary = "Get posts by user",
      description = "Retrieves all posts created by a specific user")
  public ResponseDto<PageResponse<PostWithoutUser>> getUserPosts(
      @PathVariable @Parameter(description = "User ID") Long userId,
      @ParameterObject PageRequest page) {
    PageResponse<PostWithoutUser> posts = postService.getUserPosts(userId, page);
    return ResponseDto.success(posts);
  }

  @GetMapping("/category/{categoryId}")
  @GetEndpoint(
      summary = "Get posts by category",
      description = "Retrieves all posts in a specific category")
  public ResponseDto<PageResponse<PostWithoutCategory>> getCategoryPosts(
      @PathVariable @Parameter(description = "Category ID") Long categoryId,
      @ParameterObject PageRequest page) {
    PageResponse<PostWithoutCategory> posts = postService.getPostsByCategory(categoryId, page);
    return ResponseDto.success(posts);
  }
}
