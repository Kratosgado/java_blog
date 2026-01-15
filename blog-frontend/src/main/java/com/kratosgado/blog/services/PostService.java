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

  public PageResponse<PostResponse> getPostsByAuthor(Long authorId, int page, int size) {
    ensureAuthToken();
    try {
      return postApiClient.getPostsByAuthor(authorId, page, size);
    } catch (IOException e) {
      logger.error("Failed to get posts by author due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get posts by author: {}", e.getMessage());
      throw new RuntimeException("Failed to get posts by author: " + e.getMessage(), e);
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

  // Stub methods for backward compatibility - to be implemented when needed
  public List<com.kratosgado.blog.models.Post> getPostsByUserId(Long userId) {
    logger.warn("getPostsByUserId() not yet implemented via API");
    throw new UnsupportedOperationException("getPostsByUserId not yet implemented via API");
  }

  public List<com.kratosgado.blog.models.Post> getPublishedPosts() {
    logger.warn("getPublishedPosts() not yet implemented via API - use getAllPosts with pagination");
    throw new UnsupportedOperationException("getPublishedPosts not yet implemented via API");
  }

  public List<com.kratosgado.blog.models.Post> searchPostsByKeyword(String keyword) {
    logger.warn("searchPostsByKeyword() returns PageResponse - use searchPosts() with pagination");
    throw new UnsupportedOperationException("searchPostsByKeyword not implemented - use searchPosts()");
  }

  public List<com.kratosgado.blog.models.Post> getPostsByTag(String tagName) {
    logger.warn("getPostsByTag() not yet implemented via API");
    throw new UnsupportedOperationException("getPostsByTag not yet implemented via API");
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
