package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.DuplicateResourceException;
import com.kratosgado.blog.backend.exceptions.ResourceNotFoundException;
import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.ReviewRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.models.Review;

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
      throw new ResourceNotFoundException("Post", "id", request.postId());
    }

    if (reviewRepository.existsByPostIdAndUserId(request.postId(), userId)) {
      throw new DuplicateResourceException("You have already reviewed this post");
    }

    Review review = new Review(
        request.postId(),
        userId,
        request.rating(),
        request.title(),
        request.content());

    Review savedReview = reviewRepository.save(review);
    enrichReviewWithUserData(savedReview);

    return savedReview;
  }

  @Transactional
  public Review updateReview(Long id, UpdateReviewRequest request, Long userId) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
      throw new UnauthorizedException("You are not authorized to update this review");
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
    enrichReviewWithUserData(updatedReview);

    return updatedReview;
  }

  @Transactional
  public void deleteReview(Long id, Long userId) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
      throw new UnauthorizedException("You are not authorized to delete this review");
    }

    reviewRepository.deleteById(id);

  }

  public Review getReviewById(Long id) {

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
    enrichReviewWithUserData(review);
    return review;
  }

  public Page<Review> getPostReviews(Long postId, Pageable pageable) {

    Page<Review> reviews = reviewRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    reviews.forEach(this::enrichReviewWithUserData);
    return reviews;
  }

  public Page<Review> getUserReviews(Long userId, Pageable pageable) {

    Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
    reviews.forEach(this::enrichReviewWithUserData);
    return reviews;
  }

  public Double getAverageRating(Long postId) {

    Double average = reviewRepository.getAverageRatingByPostId(postId);
    return average != null ? average : 0.0;
  }

  public Long getReviewCount(Long postId) {

    return reviewRepository.countByPostId(postId);
  }

  private void enrichReviewWithUserData(Review review) {
    userRepository.findById(review.getUserId()).ifPresent(user -> {
      review.setAuthorName(user.getUsername());
      review.setAuthorAvatarUrl(user.getAvatarUrl());
    });
  }
}
