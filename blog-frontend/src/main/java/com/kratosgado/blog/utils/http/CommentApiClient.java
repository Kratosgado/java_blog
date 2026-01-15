package com.kratosgado.blog.utils.http;

import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.CommentStatus;

import java.io.IOException;
import java.util.List;

/**
 * Client for comment REST API endpoints
 */
public class CommentApiClient extends BaseApiClient {

  public CommentApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get comments by post ID
   */
  public List<Comment> getCommentsByPostId(Long postId) throws IOException {
    logger.info("Fetching comments for post: {}", postId);
    
    HttpClient.HttpResponse<String> response = httpClient.get("/comments/post/" + postId, authToken, String.class);
    
    return handleResponse(response, List.class, "Get comments by post");
  }

  /**
   * Get comments by user ID
   */
  public List<Comment> getCommentsByUserId(Long userId) throws IOException {
    logger.info("Fetching comments by user: {}", userId);
    
    HttpClient.HttpResponse<String> response = httpClient.get("/comments/user/" + userId, authToken, String.class);
    
    return handleResponse(response, List.class, "Get comments by user");
  }

  /**
   * Get comments by status
   */
  public List<Comment> getCommentsByStatus(CommentStatus status) throws IOException {
    logger.info("Fetching comments with status: {}", status);
    
    HttpClient.HttpResponse<String> response = httpClient.get("/comments/status/" + status, authToken, String.class);
    
    return handleResponse(response, List.class, "Get comments by status");
  }

  /**
   * Create a new comment
   */
  public Comment createComment(CreateCommentRequest request) throws IOException {
    logger.info("Creating new comment on post: {}", request.postId());
    
    HttpClient.HttpResponse<String> response = httpClient.post("/comments", request, authToken, String.class);
    
    Comment comment = handleResponse(response, Comment.class, "Create comment");
    logger.info("Comment created successfully with ID: {}", comment.getId());
    return comment;
  }

  /**
   * Approve a comment
   */
  public Comment approveComment(Long id) throws IOException {
    logger.info("Approving comment ID: {}", id);
    
    HttpClient.HttpResponse<String> response = httpClient.put("/comments/" + id + "/approve", null, authToken, String.class);
    
    Comment comment = handleResponse(response, Comment.class, "Approve comment");
    logger.info("Comment approved successfully");
    return comment;
  }

  /**
   * Reject a comment
   */
  public Comment rejectComment(Long id) throws IOException {
    logger.info("Rejecting comment ID: {}", id);
    
    HttpClient.HttpResponse<String> response = httpClient.put("/comments/" + id + "/reject", null, authToken, String.class);
    
    Comment comment = handleResponse(response, Comment.class, "Reject comment");
    logger.info("Comment rejected successfully");
    return comment;
  }

  /**
   * Delete a comment
   */
  public void deleteComment(Long id) throws IOException {
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
