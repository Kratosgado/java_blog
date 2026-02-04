package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
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
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final PostRepository postRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public Review createReview(CreateReviewRequest request, User user) {
    if (!postRepository.existsById(request.postId())) {
      throw BlogException.notFound("Post", "id", request.postId());
    }

    if (reviewRepository.existsByPostIdAndUserId(request.postId(), user.getId())) {
      throw BlogException.conflict("Review already exists for this post by this user");
    }

    Review review = Review.builder()
        .postId(request.postId())
        .userId(user.getId())
        .rating(request.rating())
        .title(request.title())
        .content(request.content())
        .authorName(user.getUsername())
        .authorAvatarUrl(user.getAvatarUrl())
        .build();
    review.onCreate();

    Review saved = reviewRepository.save(review);
    log.debug("Created review with ID: {}", saved.getId());
    return saved;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public Review updateReview(String id, UpdateReviewRequest request, Long userId) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
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
    review.onUpdate();

    Review updated = reviewRepository.save(review);
    log.debug("Updated review with ID: {}", id);
    return updated;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteReview(String id, Long userId) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));

    if (!review.getUserId().equals(userId)) {
      throw BlogException.forbidden("You are not allowed to delete this review");
    }

    reviewRepository.delete(review);
    log.debug("Deleted review with ID: {}", id);
  }

  public Review getReviewById(String id) {
    return reviewRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Review", "id", id));
  }

  public PageResponse<Review> getPostReviews(Long postId, PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<Review> reviewPage = reviewRepository.findByPostId(postId, pageable);

    return DtoMapper.toPageResponse(reviewPage);
  }

  public PageResponse<ReviewWithoutUser> getUserReviews(Long userId,
      PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<ReviewWithoutUser> reviewPage = reviewRepository.findByUserId(userId, pageable);
    return DtoMapper.toPageResponse(reviewPage);

  }

  public Double getAverageRating(Long postId) {
    Double avg = reviewRepository.getAverageRating(postId);
    return avg != null ? avg : 0.0;
  }

  public Long getReviewCount(Long postId) {
    return reviewRepository.countByPostId(postId);
  }

}
