package com.kratosgado.blog.backend.services;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.ReviewResponse;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  @Transactional
  public ReviewResponse createReview(CreateReviewRequest request, Long userId) {

    if (!postRepository.existsById(request.postId())) {
      throw BlogException.notFound("Post", "id", request.postId());
    }

    if (reviewRepository.existsByPostIdAndUserId(request.postId(), userId)) {
      throw BlogException.duplicateResource("You have already reviewed this post");
    }

    Review review = new Review(
        request.postId(),
        userId,
        request.rating(),
        request.title(),
        request.content());

    Review savedReview = reviewRepository.save(review);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> BlogException.notFound("User", "id", userId));

    return DtoMapper.toReviewResponse(savedReview, user);
  }

  @Transactional
  public ReviewResponse updateReview(String id, UpdateReviewRequest request, Long userId) {

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

    Review updatedReview = reviewRepository.save(review);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> BlogException.notFound("User", "id", userId));

    return DtoMapper.toReviewResponse(updatedReview, user);
  }

  @Transactional
  public void deleteReview(String id, Long userId) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not authorized to delete this review");
    }

    reviewRepository.deleteById(id);

  }

  public ReviewResponse getReviewById(String id) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));
    User user = userRepository.findById(review.getUserId())
        .orElseThrow(() -> BlogException.notFound("User", "id", review.getUserId()));

    return DtoMapper.toReviewResponse(review, user);
  }

  public Page<ReviewResponse> getPostReviews(Long postId, Pageable pageable) {

    Page<Review> reviews = reviewRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    return enrichReviewsWithUserData(reviews);
  }

  public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {

    Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
    return enrichReviewsWithUserData(reviews);
  }

  public Double getAverageRating(Long postId) {

    var results = reviewRepository.getAverageRatingByPostId(postId);
    if (results.isEmpty()) {
      return 0.0;
    }
    Double average = results.get(0).getAvgRating();
    return average != null ? average : 0.0;
  }

  public Long getReviewCount(Long postId) {

    return reviewRepository.countByPostId(postId);
  }

  /**
   * Enriches reviews with user data using batch fetching to avoid N+1 queries.
   * Fetches all unique users in a single batch, then maps reviews to responses.
   */
  private Page<ReviewResponse> enrichReviewsWithUserData(Page<Review> reviews) {
    if (reviews.isEmpty()) {
      return new PageImpl<>(List.of(), reviews.getPageable(), 0);
    }

    // Extract unique userIds
    Set<Long> userIds = reviews.getContent().stream()
        .map(Review::getUserId)
        .collect(Collectors.toSet());

    // Batch fetch all users (1 query instead of N)
    Map<Long, User> userMap = userIds.stream()
        .map(id -> userRepository.findById(id).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(User::getId, user -> user));

    // Map to responses
    List<ReviewResponse> responses = reviews.getContent().stream()
        .map(review -> DtoMapper.toReviewResponse(
            review,
            userMap.get(review.getUserId())))
        .toList();

    return new PageImpl<>(responses, reviews.getPageable(), reviews.getTotalElements());
  }
}
