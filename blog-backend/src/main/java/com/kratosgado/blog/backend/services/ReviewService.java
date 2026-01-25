package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.utils.DtoMapper;
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

  private final ReviewRepository reviewRepository;
  private final PostRepository postRepository;
  private final UserService userService;

  public Review createReview(CreateReviewRequest request, Long userId) {

    if (!postRepository.existsById(request.postId())) {
      throw BlogException.notFound("Post", "id", request.postId());
    }

    // Check if user already reviewed this post
    if (reviewRepository.existsByPostIdAndUserId(request.postId(), userId)) {
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

    Review saved = reviewRepository.save(review);

    log.debug("Created review with ID: {}", saved.getId());
    return saved;
  }

  public Review updateReview(String id, UpdateReviewRequest request, Long userId) {

    Review review = reviewRepository.findById(id)
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

    review = reviewRepository.save(review);

    log.debug("Updated review with ID: {}", id);
    return review;
  }

  public void deleteReview(String id, Long userId) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not authorized to delete this review");
    }

    reviewRepository.deleteById(id);

    log.debug("Deleted review with ID: {}", id);
  }

  public Review getReviewById(String id) {
    return reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));
  }

  public PageResponse<Review> getPostReviews(Long postId, Pageable pageable) {
    Page<Review> reviewPage = reviewRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    return DtoMapper.toPageResponse(reviewPage, pageable);
  }

  public PageResponse<Review> getPostReviews(Long postId, int page, int size) {
    return getPostReviews(postId, PageRequest.of(page - 1, size));
  }

  public PageResponse<Review> getUserReviews(Long userId, Pageable pageable) {
    Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);
    return DtoMapper.toPageResponse(reviewPage, pageable);
  }

  public PageResponse<Review> getUserReviews(Long userId, int page, int size) {
    return getUserReviews(userId, PageRequest.of(page - 1, size));
  }

  public Double getAverageRating(Long postId) {
    var results = reviewRepository.getAverageRatingByPostId(postId);
    return results.isEmpty() ? 0.0 : results.get(0).getAvgRating();
  }

  public Long getReviewCount(Long postId) {
    return reviewRepository.countByPostId(postId);
  }
}
