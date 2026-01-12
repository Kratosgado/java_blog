package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.nosql.ReviewMongoDAO;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;

/**
 * Review service using MongoDB for flexible review storage.
 * Migrated from PostgreSQL to MongoDB for better handling of:
 * - Varying review structures (text, images, videos)
 * - Flexible metadata (votes, badges, verification status)
 * - High write throughput
 */
public class ReviewService {
  private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
  private final ReviewMongoDAO reviewMongoDAO;

  public ReviewService() {
    this.reviewMongoDAO = new ReviewMongoDAO();
    logger.info("ReviewService initialized with MongoDB backend");
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

    Optional<Review> result = reviewMongoDAO.createReview(review);
    return result.isPresent();
  }

  public List<Review> getReviewsByPostId(int postId) {
    return reviewMongoDAO.getReviewsByPostId(postId);
  }

  public List<Review> getReviewsByUserId(int userId) {
    return reviewMongoDAO.getReviewsByUserId(userId);
  }

  public List<Review> getAllReviews() {
    return reviewMongoDAO.getAllReviews();
  }

  public int getReviewCountForPost(int postId) {
    return reviewMongoDAO.getReviewCountForPost(postId);
  }

  public double getAverageRatingForPost(int postId) {
    return reviewMongoDAO.getAverageRatingForPost(postId);
  }

  public List<Review> getReviewsByRating(int rating) {
    if (rating < 1 || rating > 5) {
      throw BlogExceptions.badRequest("Rating must be between 1 and 5");
    }
    return reviewMongoDAO.getReviewsByRating(rating);
  }
}
