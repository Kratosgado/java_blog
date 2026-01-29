package com.kratosgado.blog.backend.services;

import org.springframework.stereotype.Service;

// Pageable and Page import removed, now manual pagination only.
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ReviewResponse.ReviewWithoutUser;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.User;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final PostRepository postRepository;

  public Review createReview(CreateReviewRequest request, User user) {
    // Validate post exists (JDBC)
    if (!postRepository.existsById(request.postId())) {
      throw BlogException.notFound("Post", "id", request.postId());
    }
    if (reviewRepository.existsByPostIdAndUserId(request.postId(), user.getId())) {
      throw BlogException.conflict("Review already exists for this post by this user");
    }

    Review review = new Review(request.postId(), user.getId(), request.rating(), request.title(), request.content());
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

  public PageResponse<Review> getPostReviews(Long postId, PageRequest pageRequest) {
    var reviews = reviewRepository.findByPostIdOrderByCreatedAtDesc(postId, pageRequest.getSize(),
        pageRequest.getOffset(), pageRequest.getSortBy(), pageRequest.getSortDir());
    long total = reviewRepository.countByPostId(postId);
    return DtoMapper.toPageResponse(reviews, pageRequest.getPage(), pageRequest.getSize(), (int) total);
  }

  public PageResponse<ReviewWithoutUser> getUserReviews(Long userId, PageRequest pageRequest) {
    var reviews = reviewRepository.findByUserId(userId, pageRequest.getSize(), pageRequest.getOffset(),
        pageRequest.getSortBy(), pageRequest.getSortDir());
    long total = reviewRepository.countByUserId(userId);
    return DtoMapper.toPageResponse(reviews, pageRequest.getPage(), pageRequest.getSize(), (int) total);
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
