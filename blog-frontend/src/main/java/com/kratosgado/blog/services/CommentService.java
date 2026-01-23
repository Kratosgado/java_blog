package com.kratosgado.blog.services;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.CommentApiClient;

public class CommentService {
  private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
  private final CommentApiClient commentApiClient;

  @Inject
  public CommentService() {
    this.commentApiClient = new CommentApiClient(ApiConfig.getBaseUrl());
    // Set auth token from AuthContext
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.commentApiClient.setAuthToken(token);
    }
    logger.info("CommentService initialized with API backend");
  }

  private void ensureAuthToken() {
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.commentApiClient.setAuthToken(token);
    }
  }

  public Comment createComment(CreateCommentRequest request) {
    ensureAuthToken();
    try {
      return commentApiClient.createComment(request);
    } catch (IOException e) {
      logger.error("Failed to create comment due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to create comment: {}", e.getMessage());
      throw new RuntimeException("Failed to create comment: " + e.getMessage(), e);
    }
  }

  public PageResponse<Comment> getCommentsByPostId(Long postId, int page, int size) {
    ensureAuthToken();
    try {
      return commentApiClient.getCommentsByPostId(postId, page, size);
    } catch (IOException e) {
      logger.error("Failed to get comments by post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get comments by post: {}", e.getMessage());
      throw new RuntimeException("Failed to get comments by post: " + e.getMessage(), e);
    }
  }

  /**
   * Get comments by post ID (convenience method for controllers - returns all as
   * list)
   */
  public List<Comment> getCommentsByPostId(Long postId) {
    ensureAuthToken();
    try {
      // Fetch a large page to get all comments
      PageResponse<Comment> response = commentApiClient.getCommentsByPostId(postId, 0, 1000);
      return response.content();
    } catch (IOException e) {
      logger.error("Failed to get comments by post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get comments by post: {}", e.getMessage());
      throw new RuntimeException("Failed to get comments by post: " + e.getMessage(), e);
    }
  }

  public PageResponse<Comment> getCommentsByUserId(Long userId, int page, int size) {
    ensureAuthToken();
    try {
      return commentApiClient.getCommentsByUserId(userId, page, size);
    } catch (IOException e) {
      logger.error("Failed to get comments by user due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get comments by user: {}", e.getMessage());
      throw new RuntimeException("Failed to get comments by user: " + e.getMessage(), e);
    }
  }

  /**
   * Get all comments (for admin management)
   */
  public List<Comment> getAllComments() {
    ensureAuthToken();
    try {
      // Get current user to fetch their comments (for now, admin should see all via
      // backend)
      User currentUser = AuthContext.getInstance().getCurrentUser();
      if (currentUser == null) {
        logger.warn("No current user found");
        return List.of();
      }

      // Fetch a large page to get all comments
      PageResponse<Comment> response = commentApiClient.getCommentsByUserId(currentUser.getId(), 0, 10000);
      return response.content();
    } catch (IOException e) {
      logger.error("Failed to get all comments due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get all comments: {}", e.getMessage());
      throw new RuntimeException("Failed to get all comments: " + e.getMessage(), e);
    }
  }

  /**
   * Get comment count for a post
   */
  public int getCommentCountForPost(Long postId) {
    ensureAuthToken();
    try {
      Long count = commentApiClient.getPostCommentCount(postId);
      return count != null ? count.intValue() : 0;
    } catch (IOException e) {
      logger.error("Failed to get comment count due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get comment count: {}", e.getMessage());
      throw new RuntimeException("Failed to get comment count: " + e.getMessage(), e);
    }
  }

  public Comment approveComment(String commentId) {
    ensureAuthToken();
    try {
      return commentApiClient.approveComment(commentId);
    } catch (IOException e) {
      logger.error("Failed to approve comment due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to approve comment: {}", e.getMessage());
      throw new RuntimeException("Failed to approve comment: " + e.getMessage(), e);
    }
  }

  public Comment rejectComment(String commentId) {
    ensureAuthToken();
    try {
      return commentApiClient.rejectComment(commentId);
    } catch (IOException e) {
      logger.error("Failed to reject comment due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to reject comment: {}", e.getMessage());
      throw new RuntimeException("Failed to reject comment: " + e.getMessage(), e);
    }
  }

  public void deleteComment(String commentId) {
    ensureAuthToken();
    try {
      commentApiClient.deleteComment(commentId);
    } catch (IOException e) {
      logger.error("Failed to delete comment due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to delete comment: {}", e.getMessage());
      throw new RuntimeException("Failed to delete comment: " + e.getMessage(), e);
    }
  }
}
