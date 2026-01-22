package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.dao.PostDAO;
import com.kratosgado.blog.backend.dao.nosql.ReviewMongoDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewMongoDAO reviewDAO;
  private final PostDAO postDAO;
  private final UserService userService;

  public Review createReview(CreateReviewRequest request, Long userId) {

    if (!postDAO.getPostById(request.postId().intValue()).isPresent()) {
      throw BlogException.notFound("Post", "id", request.postId());
    }

    // Check if user already reviewed this post
    List<Review> existingReviews = reviewDAO.getReviewsByPostId(request.postId());
    boolean alreadyReviewed = existingReviews.stream()
        .anyMatch(review -> review.getUserId().equals(userId));
    
    if (alreadyReviewed) {
      throw BlogException.duplicateResource("You have already reviewed this post");
    }

    User user = userService.getUserById(userId);

    Review review = new Review(
        request.postId(),
        userId,
        request.rating(),
        request.title(),
        request.content());

    // Populate author snapshot
    review.setAuthorName(user.getUsername());
    review.setAuthorAvatarUrl(user.getAvatarUrl());

    Review saved = reviewDAO.createReview(review)
        .orElseThrow(() -> BlogException.internal("Failed to create review"));
    
    log.debug("Created review with ID: {}", saved.getId());
    return saved;
  }

  public Review updateReview(String id, UpdateReviewRequest request, Long userId) {

    Review review = reviewDAO.getReviewById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not authorized to update this review");
    }

    if (request.rating() != null) {
      review.setRating(request.rating());
    }

    if (request.title() != null) {
      review.setTitle(request.title());
    }

    if (request.content() != null) {
      review.setContent(request.content());
    }
    
    if (!reviewDAO.updateReview(id, review)) {
      throw BlogException.internal("Failed to update review");
    }
    
    log.debug("Updated review with ID: {}", id);
    return review;
  }

  public void deleteReview(String id, Long userId) {

    Review review = reviewDAO.getReviewById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not authorized to delete this review");
    }

    if (!reviewDAO.deleteReview(id)) {
      throw BlogException.internal("Failed to delete review");
    }
    
    log.debug("Deleted review with ID: {}", id);
  }

  public Review getReviewById(String id) {
    return reviewDAO.getReviewById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));
  }

  public PageResponse<Review> getPostReviews(Long postId, int page, int size) {
    List<Review> reviews = reviewDAO.getReviewsByPostId(postId);
    return paginateReviews(reviews, page, size);
  }

  public PageResponse<Review> getUserReviews(Long userId, int page, int size) {
    List<Review> reviews = reviewDAO.getReviewsByUserId(userId);
    return paginateReviews(reviews, page, size);
  }

  public Double getAverageRating(Long postId) {
    return reviewDAO.getAverageRatingForPost(postId);
  }

  public Long getReviewCount(Long postId) {
    return reviewDAO.getReviewCountForPost(postId);
  }

  private PageResponse<Review> paginateReviews(List<Review> reviews, int page, int size) {
    int totalElements = reviews.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);
    
    int offset = (page - 1) * size;
    int endIndex = Math.min(offset + size, totalElements);
    
    List<Review> pagedReviews = reviews.subList(Math.max(0, offset), Math.max(0, endIndex));
    
    return new PageResponse<>(
        pagedReviews,
        page,
        size,
        totalElements,
        totalPages,
        page < totalPages,
        page > 1
    );
  }
}
