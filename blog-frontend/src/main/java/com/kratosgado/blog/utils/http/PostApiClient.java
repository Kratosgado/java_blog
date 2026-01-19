package com.kratosgado.blog.utils.http;

import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Post;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Client for post REST API endpoints
 */
public class PostApiClient extends BaseApiClient {

  public PostApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get all posts with pagination
   */
  public PageResponse<PostResponse> getAllPosts(int page, int size) throws IOException {
    logger.info("Fetching posts - page: {}, size: {}", page, size);
    
    String endpoint = String.format("/posts?page=%d&size=%d", page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);
    
    Type pageResponseType = TypeToken.getParameterized(PageResponse.class, PostResponse.class).getType();
    return handleResponse(response, pageResponseType, "Get posts");
  }

  /**
   * Get post by ID
   */
  public PostResponse getPostById(Long id) throws IOException {
    logger.info("Fetching post by ID: {}", id);
    
    HttpClient.HttpResponse<String> response = httpClient.get("/posts/" + id, authToken, String.class);
    
    return handleResponse(response, PostResponse.class, "Get post by ID");
  }

  /**
   * Get posts by category
   */
  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, int page, int size) throws IOException {
    logger.info("Fetching posts by category: {}", categoryId);
    
    String endpoint = String.format("/posts/category/%d?page=%d&size=%d", categoryId, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);
    
    Type pageResponseType = TypeToken.getParameterized(PageResponse.class, PostResponse.class).getType();
    return handleResponse(response, pageResponseType, "Get posts by category");
  }

  /**
   * Get posts by user
   */
  public PageResponse<PostResponse> getPostsByUser(Long userId, int page, int size) throws IOException {
    logger.info("Fetching posts by user: {}", userId);
    
    String endpoint = String.format("/posts/user/%d?page=%d&size=%d", userId, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);
    
    Type pageResponseType = TypeToken.getParameterized(PageResponse.class, PostResponse.class).getType();
    return handleResponse(response, pageResponseType, "Get posts by user");
  }

  /**
   * Search posts by title or content
   */
  public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) throws IOException {
    logger.info("Searching posts with keyword: {}", keyword);
    
    String endpoint = String.format("/posts/search?keyword=%s&page=%d&size=%d", keyword, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);
    
    Type pageResponseType = TypeToken.getParameterized(PageResponse.class, PostResponse.class).getType();
    return handleResponse(response, pageResponseType, "Search posts");
  }

  /**
   * Create a new post
   */
  public Post createPost(CreatePostRequest request) throws IOException {
    logger.info("Creating new post: {}", request.title());
    
    HttpClient.HttpResponse<String> response = httpClient.post("/posts", request, authToken, String.class);
    
    Post post = handleResponse(response, Post.class, "Create post");
    logger.info("Post created successfully with ID: {}", post.getId());
    return post;
  }

  /**
   * Update an existing post
   */
  public Post updatePost(Long id, UpdatePostRequest request) throws IOException {
    logger.info("Updating post ID: {}", id);
    
    HttpClient.HttpResponse<String> response = httpClient.put("/posts/" + id, request, authToken, String.class);
    
    Post post = handleResponse(response, Post.class, "Update post");
    logger.info("Post updated successfully");
    return post;
  }

  /**
   * Delete a post
   */
  public void deletePost(Long id) throws IOException {
    logger.info("Deleting post ID: {}", id);
    
    HttpClient.HttpResponse<String> response = httpClient.delete("/posts/" + id, authToken, String.class);
    
    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Delete post failed: {}", errorMessage);
      throw new ApiException("Delete post failed: " + errorMessage, response.getStatusCode());
    }
    
    logger.info("Post deleted successfully");
  }
}
