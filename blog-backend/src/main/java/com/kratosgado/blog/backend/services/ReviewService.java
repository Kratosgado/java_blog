package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.models.Review;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ReviewResponse.ReviewWithoutUser;

public interface ReviewService {

  Review createReview(CreateReviewRequest request, User user);

  Review updateReview(String id, UpdateReviewRequest request, Long userId);

  void deleteReview(String id, Long userId);

  Review getReviewById(String id);

  PageResponse<Review> getPostReviews(Long postId, PageRequest pageRequest);

  PageResponse<ReviewWithoutUser> getUserReviews(Long userId, PageRequest pageRequest);

  Double getAverageRating(Long postId);

  Long getReviewCount(Long postId);
}
