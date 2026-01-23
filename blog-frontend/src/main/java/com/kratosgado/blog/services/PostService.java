package com.kratosgado.blog.services;

import java.io.IOException;
import java.util.List;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.PostApiClient;

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

  public Post createPost(CreatePostRequest request) {
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

  public Post updatePost(Long id, UpdatePostRequest request) {
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

  public PostResponse getPostById(Long id) {
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

  public PageResponse<PostResponse> getPostsByUser(Long userId, int page, int size) {
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

  public PageResponse<PostResponse> getAllPosts(int page, int size) {
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

  public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) {
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

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, int page, int size) {
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

  // Convenience methods for controllers that expect List<Post> instead of PageResponse
  
  /**
   * Get all published posts as a list (for controllers)
   */
  public List<Post> getPublishedPosts() {
    ensureAuthToken();
    try {
      PageResponse<PostResponse> response = postApiClient.getAllPosts(0, 1000);
      // Convert PostResponse to Post (they should be compatible or you need mapping)
      return response.content().stream()
          .map(this::convertToPost)
          .toList();
    } catch (IOException e) {
      logger.error("Failed to get published posts due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get published posts: {}", e.getMessage());
      throw new RuntimeException("Failed to get published posts: " + e.getMessage(), e);
    }
  }

  /**
   * Get posts by user ID as a list (for controllers)
   */
  public List<Post> getPostsByUserId(Long userId) {
    ensureAuthToken();
    try {
      PageResponse<PostResponse> response = postApiClient.getPostsByUser(userId, 0, 1000);
      return response.content().stream()
          .map(this::convertToPost)
          .toList();
    } catch (IOException e) {
      logger.error("Failed to get posts by user due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get posts by user: {}", e.getMessage());
      throw new RuntimeException("Failed to get posts by user: " + e.getMessage(), e);
    }
  }

  /**
   * Search posts by keyword as a list (for controllers)
   */
  public List<Post> searchPostsByKeyword(String keyword) {
    ensureAuthToken();
    try {
      PageResponse<PostResponse> response = postApiClient.searchPosts(keyword, 0, 1000);
      return response.content().stream()
          .map(this::convertToPost)
          .toList();
    } catch (IOException e) {
      logger.error("Failed to search posts due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to search posts: {}", e.getMessage());
      throw new RuntimeException("Failed to search posts: " + e.getMessage(), e);
    }
  }

  /**
   * Get posts by tag name (stub - not yet implemented in backend)
   */
  public List<Post> getPostsByTag(String tagName) {
    logger.warn("getPostsByTag() not yet fully implemented via API");
    // Return empty list for now - this would need a new backend endpoint
    return List.of();
  }

  /**
   * Convert PostResponse to Post model
   * This is a temporary mapping until we unify the models
   */
  private Post convertToPost(PostResponse response) {
    Post post = new Post();
    post.setId(response.id());
    post.setTitle(response.title());
    post.setContent(response.content());
    post.setExcerpt(response.excerpt());
    post.setCoverImage(response.coverImage());
    post.setStatus(response.status() != null ? response.status().toString() : "DRAFT");
    post.setUserId(response.authorId()); // PostResponse uses authorId, Post uses userId
    post.setCategoryId(response.categoryId());
    post.setCreatedAt(response.createdAt());
    post.setUpdatedAt(response.updatedAt());
    post.setViews(response.views());
    post.setLikesCount(response.likesCount());
    post.setAuthorName(response.authorName());
    post.setAuthorAvatarUrl(response.authorAvatarUrl());
    return post;
  }

  public boolean incrementViews(Long postId) {
    logger.warn("incrementViews() not yet implemented via API");
    throw new UnsupportedOperationException("incrementViews not yet implemented via API");
  }

  public boolean publishPost(Long postId) {
    logger.warn("publishPost() not yet implemented via API");
    throw new UnsupportedOperationException("publishPost not yet implemented via API");
  }
}
