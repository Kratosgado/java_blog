package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
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
import com.kratosgado.blog.dtos.response.PageResponse;
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
  public Review createReview(CreateReviewRequest request, Long userId) {

    if (!postRepository.existsById(request.postId())) {
      throw BlogException.notFound("Post", "id", request.postId());
    }

    if (reviewRepository.existsByPostIdAndUserId(request.postId(), userId)) {
      throw BlogException.duplicateResource("You have already reviewed this post");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> BlogException.notFound("User", "id", userId));

    Review review = new Review(
        request.postId(),
        userId,
        request.rating(),
        request.title(),
        request.content());

    // Populate author snapshot
    review.setAuthorName(user.getUsername());
    review.setAuthorAvatarUrl(user.getAvatarUrl());

    return reviewRepository.save(review);

  }

  @Transactional
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
    return reviewRepository.save(review);

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

  public Review getReviewById(String id) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));
    return review;

  }

  public PageResponse<Review> getPostReviews(Long postId, Pageable pageable) {

    Page<Review> reviews = reviewRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    return DtoMapper.toPageResponse(reviews, pageable);
  }

  public PageResponse<Review> getUserReviews(Long userId, Pageable pageable) {

    Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
    return DtoMapper.toPageResponse(reviews, pageable);
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
}
