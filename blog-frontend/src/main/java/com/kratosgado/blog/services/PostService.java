package com.kratosgado.blog.services;

import com.google.inject.Inject;
import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.PostApiClient;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostService {
  private static final Logger logger = LoggerFactory.getLogger(PostService.class);
  private final PostApiClient postApiClient;

  @Inject
  public PostService() {
    this.postApiClient = new PostApiClient(ApiConfig.getBaseUrl());
    // Set auth token from AuthContext
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.postApiClient.setAuthToken(token);
    }
  }

  private void ensureAuthToken() {
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.postApiClient.setAuthToken(token);
    }
  }

  public PostResponse.PostDetails createPost(CreatePostRequest request) {
    ensureAuthToken();
    try {
      return postApiClient.createPost(request);
    } catch (IOException e) {
      logger.error("Failed to create post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to create post: {}", e.getMessage());
      throw new RuntimeException("Failed to create post: " + e.getMessage(), e);
    }
  }

  public PostResponse.PostDetails updatePost(Long id, UpdatePostRequest request) {
    ensureAuthToken();
    try {
      return postApiClient.updatePost(id, request);
    } catch (IOException e) {
      logger.error("Failed to update post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to update post: {}", e.getMessage());
      throw new RuntimeException("Failed to update post: " + e.getMessage(), e);
    }
  }

  public void deletePost(Long id) {
    ensureAuthToken();
    try {
      postApiClient.deletePost(id);
    } catch (IOException e) {
      logger.error("Failed to delete post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to delete post: {}", e.getMessage());
      throw new RuntimeException("Failed to delete post: " + e.getMessage(), e);
    }
  }

  public PostResponse.PostDetails getPostById(Long id) {
    ensureAuthToken();
    try {
      return postApiClient.getPostById(id);
    } catch (IOException e) {
      logger.error("Failed to get post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get post: {}", e.getMessage());
      throw new RuntimeException("Failed to get post: " + e.getMessage(), e);
    }
  }

  public PageResponse<PostResponse.PostWithoutUser> getPostsByUser(Long userId, int page, int size) {
    ensureAuthToken();
    try {
      return postApiClient.getPostsByUser(userId, page, size);
    } catch (IOException e) {
      logger.error("Failed to get posts by user due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get posts by user: {}", e.getMessage());
      throw new RuntimeException("Failed to get posts by user: " + e.getMessage(), e);
    }
  }

  public PageResponse<PostResponse.PostView> getAllPosts(int page, int size) {
    ensureAuthToken();
    try {
      return postApiClient.getAllPosts(page, size);
    } catch (IOException e) {
      logger.error("Failed to get posts due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get posts: {}", e.getMessage());
      throw new RuntimeException("Failed to get posts: " + e.getMessage(), e);
    }
  }

  public PageResponse<PostResponse.PostView> searchPosts(String keyword, int page, int size) {
    ensureAuthToken();
    try {
      return postApiClient.searchPosts(keyword, page, size);
    } catch (IOException e) {
      logger.error("Failed to search posts due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to search posts: {}", e.getMessage());
      throw new RuntimeException("Failed to search posts: " + e.getMessage(), e);
    }
  }

  public PageResponse<PostWithoutCategory> getPostsByCategory(Long categoryId, int page, int size) {
    ensureAuthToken();
    try {
      return postApiClient.getPostsByCategory(categoryId, page, size);
    } catch (IOException e) {
      logger.error("Failed to get posts by category due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get posts by category: {}", e.getMessage());
      throw new RuntimeException("Failed to get posts by category: " + e.getMessage(), e);
    }
  }

  // Convenience methods for controllers that expect List<Post> instead of
  // PageResponse

  /** Get all published posts as a list (for controllers) */
  public List<Post> getPublishedPosts() {
    ensureAuthToken();
    try {
      PageResponse<PostResponse.PostView> response = postApiClient.getAllPosts(0, 1000);
      // Convert PostView to Post
      return response.content().stream().map(this::convertPostViewToPost).toList();
    } catch (IOException e) {
      logger.error("Failed to get published posts due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get published posts: {}", e.getMessage());
      throw new RuntimeException("Failed to get published posts: " + e.getMessage(), e);
    }
  }

  /** Get posts by user ID as a list (for controllers) */
  public List<Post> getPostsByUserId(Long userId) {
    ensureAuthToken();
    try {
      PageResponse<PostResponse.PostWithoutUser> response = postApiClient.getPostsByUser(userId, 0, 1000);
      return response.content().stream().map(this::convertPostWithoutUserToPost).toList();
    } catch (IOException e) {
      logger.error("Failed to get posts by user due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get posts by user: {}", e.getMessage());
      throw new RuntimeException("Failed to get posts by user: " + e.getMessage(), e);
    }
  }

  /** Get total views for all posts by a user */
  public long getTotalViews(Long userId) {
    List<Post> posts = getPostsByUserId(userId);
    return posts.stream().mapToLong(p -> p.getViews() != null ? p.getViews() : 0).sum();
  }

  /** Search posts by keyword as a list (for controllers) */
  public List<Post> searchPostsByKeyword(String keyword) {
    ensureAuthToken();
    try {
      PageResponse<PostResponse.PostView> response = postApiClient.searchPosts(keyword, 0, 1000);
      return response.content().stream().map(this::convertPostViewToPost).toList();
    } catch (IOException e) {
      logger.error("Failed to search posts due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to search posts: {}", e.getMessage());
      throw new RuntimeException("Failed to search posts: " + e.getMessage(), e);
    }
  }

  /** Get posts by tag name (stub - not yet implemented in backend) */
  public List<Post> getPostsByTag(String tagName) {
    logger.warn("getPostsByTag() not yet fully implemented via API");
    // Return empty list for now - this would need a new backend endpoint
    return List.of();
  }

  public boolean publishPost(Long postId) {
    ensureAuthToken();
    try {
      postApiClient.publishPost(postId);
      return true;
    } catch (IOException e) {
      logger.error("Failed to publish post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to publish post: {}", e.getMessage());
      throw new RuntimeException("Failed to publish post: " + e.getMessage(), e);
    }
  }

  public boolean likePost(Long postId) {
    logger.warn("likePost() not yet implemented via API");
    // For now we just return true to simulate UI success
    return true;
  }

  /** Convert PostView projection to Post entity */
  private Post convertPostViewToPost(PostResponse.PostView postView) {
    Post post = new Post();
    post.setId(postView.getId());
    post.setTitle(postView.getTitle());
    post.setSlug(postView.getSlug());
    post.setExcerpt(postView.getExcerpt());
    post.setStatus(postView.getStatus());
    post.setCoverImage(postView.getCoverImage());
    post.setCreatedAt(postView.getCreatedAt());
    post.setViews(postView.getViews());
    post.setLikesCount(postView.getLikesCount());

    // Set user if available
    if (postView.getUser() != null) {
      var userSummary = postView.getUser();
      post.setUserId(userSummary.getId());
      com.kratosgado.blog.models.User user = new com.kratosgado.blog.models.User();
      user.setId(userSummary.getId());
      user.setUsername(userSummary.getUsername());
      user.setAvatarUrl(userSummary.getAvatarUrl());
      post.setUser(user);
    }

    // Set category if available
    if (postView.getCategory() != null) {
      var categorySummary = postView.getCategory();
      post.setCategory(com.kratosgado.blog.models.Category.builder()
          .id(categorySummary.getId())
          .name(categorySummary.getName())
          .slug(categorySummary.getSlug())
          .build());
    }

    return post;
  }

  /** Convert PostWithoutUser projection to Post entity */
  private Post convertPostWithoutUserToPost(PostResponse.PostWithoutUser postWithoutUser) {
    Post post = new Post();
    post.setId(postWithoutUser.getId());
    post.setTitle(postWithoutUser.getTitle());
    post.setSlug(postWithoutUser.getSlug());
    post.setExcerpt(postWithoutUser.getExcerpt());
    post.setStatus(postWithoutUser.getStatus());
    post.setCoverImage(postWithoutUser.getCoverImage());
    post.setCreatedAt(postWithoutUser.getCreatedAt());
    post.setViews(postWithoutUser.getViews());
    post.setLikesCount(postWithoutUser.getLikesCount());

    // Set category if available
    if (postWithoutUser.getCategory() != null) {
      var categorySummary = postWithoutUser.getCategory();
      post.setCategory(com.kratosgado.blog.models.Category.builder()
          .id(categorySummary.getId())
          .name(categorySummary.getName())
          .slug(categorySummary.getSlug())
          .build());
    }

    return post;
  }
}
