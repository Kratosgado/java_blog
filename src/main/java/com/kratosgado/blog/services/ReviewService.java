package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.ReviewDAO;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;

public class ReviewService {
  private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
  private final ReviewDAO reviewDAO;

  @Inject
  public ReviewService(ReviewDAO reviewDAO) {
    this.reviewDAO = reviewDAO;
  }

  public boolean createReview(Review review) {
    // Validate rating
    if (review.getRating() < 1 || review.getRating() > 5) {
      throw BlogExceptions.badRequest("Rating must be between 1 and 5 stars");
    }

    // Validate title
    if (review.getTitle() != null && review.getTitle().length() > 255) {
      throw BlogExceptions.badRequest("Review title is too long (max 255 characters)");
    }

    // Validate content
    if (review.getContent() != null && review.getContent().length() > 5000) {
      throw BlogExceptions.badRequest("Review content is too long (max 5000 characters)");
    }

    return reviewDAO.createReview(review);
  }

  public boolean updateReview(Review review) {
    Optional<Review> existing = reviewDAO.getReviewById(review.getId());
    if (existing.isEmpty()) {
      throw BlogExceptions.notFound("Review not found");
    }

    // Validate rating
    if (review.getRating() < 1 || review.getRating() > 5) {
      throw BlogExceptions.badRequest("Rating must be between 1 and 5 stars");
    }

    return reviewDAO.updateReview(review);
  }

  public boolean deleteReview(int id) {
    Optional<Review> review = reviewDAO.getReviewById(id);
    if (review.isEmpty()) {
      throw BlogExceptions.notFound("Review not found");
    }
    return reviewDAO.deleteReview(id);
  }

  public Optional<Review> getReviewById(int id) {
    return reviewDAO.getReviewById(id);
  }

  public List<Review> getReviewsByPostId(int postId) {
    return reviewDAO.getReviewsByPostId(postId);
  }

  public List<Review> getReviewsByUserId(int userId) {
    return reviewDAO.getReviewsByUserId(userId);
  }

  public List<Review> getAllReviews() {
    return reviewDAO.getAllReviews();
  }

  public int getReviewCountForPost(int postId) {
    return reviewDAO.getReviewCountForPost(postId);
  }

  public double getAverageRatingForPost(int postId) {
    return reviewDAO.getAverageRatingForPost(postId);
  }

  public List<Review> getReviewsByRating(int rating) {
    if (rating < 1 || rating > 5) {
      throw BlogExceptions.badRequest("Rating must be between 1 and 5");
    }
    return reviewDAO.getReviewsByRating(rating);
  }

  public List<Review> getHelpfulReviews() {
    return reviewDAO.getHelpfulReviews();
  }
}
