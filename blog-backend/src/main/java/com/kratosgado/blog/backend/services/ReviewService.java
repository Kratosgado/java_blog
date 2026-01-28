package com.kratosgado.blog.backend.services;

import java.sql.SQLException;

import org.springframework.stereotype.Service;
// Pageable and Page import removed, now manual pagination only.
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ReviewResponse.ReviewWithoutUser;
import com.kratosgado.blog.models.Review;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final PostRepository postRepository;
  private final UserService userService;

  public ReviewService(ReviewRepository reviewRepository, PostRepository postRepository, UserService userService) {
    this.reviewRepository = reviewRepository;
    this.postRepository = postRepository;
    this.userService = userService;
  }

  public Review createReview(CreateReviewRequest request, Long userId) {
    // Validate post exists (JDBC)
    postRepository.findById(request.postId())
        .orElseThrow(() -> BlogException.notFound("Post", "id", request.postId()));

    if (reviewRepository.existsByPostIdAndUserId(request.postId(), userId)) {
      throw BlogException.conflict("Review already exists for this post by this user");
    }

    var user = userService.getUserById(userId);
    Review review = new Review(request.postId(), userId, request.rating(), request.title(), request.content());
    review.setAuthorName(user.getUsername());
    review.setAuthorAvatarUrl(user.getAvatarUrl());

    Review saved = reviewRepository.save(review);
    log.debug("Created review with ID: {}", saved.getId());
    return saved;
  }

  public Review updateReview(String id, UpdateReviewRequest request, Long userId) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (review.getUserId() == null || !review.getUserId().equals(userId)) {
      throw BlogException.forbidden("You are not allowed to update this review");
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

    review.setUpdatedAt();
    Review updated = reviewRepository.save(review);
    log.debug("Updated review with ID: {}", id);
    return updated;
  }

  public void deleteReview(String id, Long userId) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (review.getUserId() == null || !review.getUserId().equals(userId)) {
      throw BlogException.forbidden("You are not allowed to delete this review");
    }

    reviewRepository.deleteById(id);
    log.debug("Deleted review with ID: {}", id);
  }

  public Review getReviewById(String id) {
    return reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));
  }

  public PageResponse<Review> getPostReviews(Long postId, int page, int size) {
    var reviews = reviewRepository.findByPostIdOrderByCreatedAtDesc(postId, size, page * size);
    long total = reviewRepository.countByPostId(postId);
    return DtoMapper.toPageResponse(reviews, size, page, (int) total);
  }

  public PageResponse<ReviewWithoutUser> getUserReviews(Long userId, int page, int size) {
    var reviews = reviewRepository.findByUserId(userId, size, page * size);
    long total = reviewRepository.countByUserId(userId);
    return DtoMapper.toPageResponse(reviews, size, page, (int) total);
  }

  public Double getAverageRating(Long postId) {
    var results = reviewRepository.getAverageRatingByPostId(postId);
    if (results == null || results.isEmpty() || results.get(0).avgRating() == null) {
      return 0.0;
    }
    return results.get(0).avgRating();
  }

  public Long getReviewCount(Long postId) {
    return reviewRepository.countByPostId(postId);
  }
}
