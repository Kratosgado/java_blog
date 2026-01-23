package com.kratosgado.blog.utils.http;

import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.models.Review;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Client for review REST API endpoints
 */
public class ReviewApiClient extends BaseApiClient {

  public ReviewApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Create a new review
   */
  public Review createReview(CreateReviewRequest request) throws IOException {
    logger.info("Creating new review for post: {}", request.postId());

    HttpClient.HttpResponse<String> response = httpClient.post("/reviews", request, authToken, String.class);

    Review review = handleResponse(response, Review.class, "Create review");
    logger.info("Review created successfully with ID: {}", review.getId());
    return review;
  }

  /**
   * Update an existing review
   */
  public Review updateReview(String id, UpdateReviewRequest request) throws IOException {
    logger.info("Updating review ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/reviews/" + id, request, authToken, String.class);

    Review review = handleResponse(response, Review.class, "Update review");
    logger.info("Review updated successfully");
    return review;
  }

  /**
   * Delete a review
   */
  public void deleteReview(String id) throws IOException {
    logger.info("Deleting review ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.delete("/reviews/" + id, authToken, String.class);

    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Delete review failed: {}", errorMessage);
      throw new ApiException("Delete review failed: " + errorMessage, response.getStatusCode());
    }

    logger.info("Review deleted successfully");
  }

  /**
   * Get review by ID
   */
  public Review getReviewById(String id) throws IOException {
    logger.info("Fetching review by ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.get("/reviews/" + id, authToken, String.class);

    return handleResponse(response, Review.class, "Get review by ID");
  }

  /**
   * Get reviews for a specific post with pagination
   */
  public PageResponse<Review> getPostReviews(Long postId, int page, int size) throws IOException {
    logger.info("Fetching reviews for post: {}", postId);

    String endpoint = String.format("/reviews/post/%d?page=%d&size=%d", postId, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageType = new TypeToken<PageResponse<Review>>() {
    }.getType();
    return gson.fromJson(response.getRawBody(), pageType);
  }

  /**
   * Get reviews by a specific user with pagination
   */
  public PageResponse<Review> getUserReviews(Long userId, int page, int size) throws IOException {
    logger.info("Fetching reviews by user: {}", userId);

    String endpoint = String.format("/reviews/user/%d?page=%d&size=%d", userId, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageType = new TypeToken<PageResponse<Review>>() {
    }.getType();
    return gson.fromJson(response.getRawBody(), pageType);
  }

  /**
   * Get review statistics for a post
   */
  public Map<String, Object> getPostReviewStats(Long postId) throws IOException {
    logger.info("Fetching review statistics for post: {}", postId);

    String endpoint = String.format("/reviews/post/%d/stats", postId);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Get review stats failed: {}", errorMessage);
      throw new ApiException("Get review stats failed: " + errorMessage, response.getStatusCode());
    }

    Type responseType = new TypeToken<ResponseDto<Map<String, Object>>>() {
    }.getType();
    ResponseDto<Map<String, Object>> apiResponse = gson.fromJson(response.getRawBody(), responseType);

    if (!"success".equals(apiResponse.status())) {
      throw new ApiException(apiResponse.message(), response.getStatusCode());
    }

    return apiResponse.data();
  }
}
