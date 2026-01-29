package com.kratosgado.blog.utils.http;

import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Comment;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Client for comment REST API endpoints
 */
public class CommentApiClient extends BaseApiClient {

  public CommentApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get comments by post ID with pagination
   */
  public PageResponse<Comment> getCommentsByPostId(Long postId, int page, int size) throws IOException {
    logger.info("Fetching comments for post: {} - page: {}, size: {}", postId, page, size);

    String endpoint = String.format("/comments/post/%d?page=%d&size=%d", postId, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageResponseType = TypeToken.getParameterized(PageResponse.class, Comment.class).getType();
    return handleResponse(response, pageResponseType, "Get post comments");
  }

  /**
   * Get comments by user ID with pagination
   */
  public PageResponse<Comment> getCommentsByUserId(Long userId, int page, int size) throws IOException {
    logger.info("Fetching comments by user: {} - page: {}, size: {}", userId, page, size);

    String endpoint = String.format("/comments/user/%d?page=%d&size=%d", userId, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageResponseType = TypeToken.getParameterized(PageResponse.class, Comment.class).getType();
    return handleResponse(response, pageResponseType, "Get user comments");
  }

  /**
   * Get post comment count
   */
  public Long getPostCommentCount(Long postId) throws IOException {
    logger.info("Fetching comment count for post: {}", postId);

    String endpoint = String.format("/comments/post/%d/count", postId);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    return handleResponse(response, Long.class, "Get post comment count");
  }

  /**
   * Create a new comment
   */
  public Comment createComment(CreateCommentRequest request) throws IOException {
    logger.info("Creating new comment on post: {}", request.postId());

    HttpClient.HttpResponse<String> response = httpClient.post("/comments", request, authToken, String.class);

    return handleResponse(response, Comment.class, "Create comment");
  }

  /**
   * Approve a comment
   */
  public Comment approveComment(String id) throws IOException {
    logger.info("Approving comment ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/comments/" + id + "/approve", null, authToken,
        String.class);

    return handleResponse(response, Comment.class, "Approve comment");
  }

  /**
   * Reject a comment
   */
  public Comment rejectComment(String id) throws IOException {
    logger.info("Rejecting comment ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/comments/" + id + "/reject", null, authToken,
        String.class);

    return handleResponse(response, Comment.class, "Reject comment");
  }

  /**
   * Get a comment by ID
   */
  public Comment getCommentById(String id) throws IOException {
    logger.info("Fetching comment by ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.get("/comments/" + id, authToken, String.class);

    return handleResponse(response, Comment.class, "Get comment by ID");
  }

  /**
   * Delete a comment
   */
  public void deleteComment(String id) throws IOException {
    logger.info("Deleting comment ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.delete("/comments/" + id, authToken, String.class);

    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Delete comment failed: {}", errorMessage);
      throw new ApiException("Delete comment failed: " + errorMessage, response.getStatusCode());
    }

    logger.info("Comment deleted successfully");
  }
}
