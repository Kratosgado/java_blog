package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dao.nosql.ReviewMongoDAO;
import com.kratosgado.blog.dtos.request.CreateReviewDto;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidatorEngine;

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
  private final UserDAO userDAO;

  public ReviewService() {
    this.reviewMongoDAO = new ReviewMongoDAO();
    this.userDAO = new UserDAO();
    logger.info("ReviewService initialized with MongoDB backend");
  }

  public boolean createReview(CreateReviewDto dto) {
    ValidatorEngine.validate(dto);
    
    // Validate rating range (1-5)
    if (dto.rating() < 1 || dto.rating() > 5) {
      throw BlogExceptions.badRequest("Rating must be between 1 and 5 stars");
    }

    Review review = new Review(dto.postId(), dto.userId(), dto.rating(), dto.title(), dto.content());
    
    // Fetch user information to populate author name and avatar
    Optional<User> userOpt = userDAO.getUserById(dto.userId());
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      review.setAuthorName(user.getUsername());
      review.setAuthorAvatarUrl(user.getAvatarUrl());
      logger.debug("Set author details for review: {} ({})", user.getUsername(), user.getAvatarUrl());
    } else {
      logger.warn("User not found for userId: {}, review will have no author info", dto.userId());
    }

    Optional<Review> result = reviewMongoDAO.createReview(review);
    if (result.isPresent()) {
      logger.info("Review created successfully for post: {}", dto.postId());
      return true;
    }
    return false;
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
