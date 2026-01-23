package com.kratosgado.blog.services;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.ReviewApiClient;

/**
 * Service for handling review operations via REST API
 */
public class ReviewService {
  private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
  private final ReviewApiClient reviewApiClient;

  @Inject
  public ReviewService() {
    this.reviewApiClient = new ReviewApiClient(ApiConfig.getBaseUrl());
    // Set auth token from AuthContext
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.reviewApiClient.setAuthToken(token);
    }
    logger.info("ReviewService initialized with API backend");
  }

  private void ensureAuthToken() {
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.reviewApiClient.setAuthToken(token);
    }
  }

  public Review createReview(CreateReviewRequest request) {
    ensureAuthToken();
    try {
      return reviewApiClient.createReview(request);
    } catch (IOException e) {
      logger.error("Failed to create review due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to create review: {}", e.getMessage());
      throw new RuntimeException("Failed to create review: " + e.getMessage(), e);
    }
  }

  public Review updateReview(String id, UpdateReviewRequest request) {
    ensureAuthToken();
    try {
      return reviewApiClient.updateReview(id, request);
    } catch (IOException e) {
      logger.error("Failed to update review due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to update review: {}", e.getMessage());
      throw new RuntimeException("Failed to update review: " + e.getMessage(), e);
    }
  }

  public void deleteReview(String id) {
    ensureAuthToken();
    try {
      reviewApiClient.deleteReview(id);
    } catch (IOException e) {
      logger.error("Failed to delete review due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to delete review: {}", e.getMessage());
      throw new RuntimeException("Failed to delete review: " + e.getMessage(), e);
    }
  }

  public Review getReviewById(String id) {
    ensureAuthToken();
    try {
      return reviewApiClient.getReviewById(id);
    } catch (IOException e) {
      logger.error("Failed to get review due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get review: {}", e.getMessage());
      throw new RuntimeException("Failed to get review: " + e.getMessage(), e);
    }
  }

  public PageResponse<Review> getPostReviews(Long postId, int page, int size) {
    ensureAuthToken();
    try {
      return reviewApiClient.getPostReviews(postId, page, size);
    } catch (IOException e) {
      logger.error("Failed to get post reviews due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get post reviews: {}", e.getMessage());
      throw new RuntimeException("Failed to get post reviews: " + e.getMessage(), e);
    }
  }

  public PageResponse<Review> getUserReviews(Long userId, int page, int size) {
    ensureAuthToken();
    try {
      return reviewApiClient.getUserReviews(userId, page, size);
    } catch (IOException e) {
      logger.error("Failed to get user reviews due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get user reviews: {}", e.getMessage());
      throw new RuntimeException("Failed to get user reviews: " + e.getMessage(), e);
    }
  }

  public Map<String, Object> getPostReviewStats(Long postId) {
    ensureAuthToken();
    try {
      return reviewApiClient.getPostReviewStats(postId);
    } catch (IOException e) {
      logger.error("Failed to get review stats due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get review stats: {}", e.getMessage());
      throw new RuntimeException("Failed to get review stats: " + e.getMessage(), e);
    }
  }
}
