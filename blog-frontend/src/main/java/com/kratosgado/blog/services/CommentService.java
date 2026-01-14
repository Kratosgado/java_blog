package com.kratosgado.blog.services;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.CommentStatus;
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

  public List<Comment> getCommentsByPostId(Long postId) {
    ensureAuthToken();
    try {
      return commentApiClient.getCommentsByPostId(postId);
    } catch (IOException e) {
      logger.error("Failed to get comments by post due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get comments by post: {}", e.getMessage());
      throw new RuntimeException("Failed to get comments by post: " + e.getMessage(), e);
    }
  }

  public List<Comment> getCommentsByUserId(Long userId) {
    ensureAuthToken();
    try {
      return commentApiClient.getCommentsByUserId(userId);
    } catch (IOException e) {
      logger.error("Failed to get comments by user due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get comments by user: {}", e.getMessage());
      throw new RuntimeException("Failed to get comments by user: " + e.getMessage(), e);
    }
  }

  public List<Comment> getCommentsByStatus(CommentStatus status) {
    ensureAuthToken();
    try {
      return commentApiClient.getCommentsByStatus(status);
    } catch (IOException e) {
      logger.error("Failed to get comments by status due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get comments by status: {}", e.getMessage());
      throw new RuntimeException("Failed to get comments by status: " + e.getMessage(), e);
    }
  }

  public List<Comment> getPendingComments() {
    return getCommentsByStatus(CommentStatus.PENDING);
  }

  public List<Comment> getApprovedComments() {
    return getCommentsByStatus(CommentStatus.APPROVED);
  }

  public List<Comment> getRejectedComments() {
    return getCommentsByStatus(CommentStatus.REJECTED);
  }

  public Comment approveComment(Long commentId) {
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

  public Comment rejectComment(Long commentId) {
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

  public void deleteComment(Long commentId) {
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

  // Stub methods for backward compatibility - to be implemented when API is ready
  public List<Comment> getAllComments() {
    logger.warn("getAllComments() not yet implemented via API");
    throw new UnsupportedOperationException("getAllComments not yet implemented via API");
  }

  public int getCommentCountForPost(Long postId) {
    logger.warn("getCommentCountForPost() not yet implemented via API");
    return getCommentsByPostId(postId).size(); // Temporary workaround
  }

  public int getApprovedCommentCountForPost(Long postId) {
    logger.warn("getApprovedCommentCountForPost() not yet implemented via API");
    return (int) getCommentsByPostId(postId).stream()
        .filter(c -> c.getStatus() == CommentStatus.APPROVED)
        .count(); // Temporary workaround
  }
}
