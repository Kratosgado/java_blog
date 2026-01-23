package com.kratosgado.blog.backend.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.models.Review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  @Operation(summary = "Create a new review", description = "Creates a new review for a post. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredCreateEndpoint
  public ResponseDto<Review> createReview(
      @Valid @RequestBody @Parameter(description = "Review creation request") CreateReviewRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Review review = reviewService.createReview(request, userId);
    return ResponseDto.success("Review created successfully", review);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a review", description = "Updates an existing review. Only the review author can update it.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredUpdateEndpoint
  public ResponseDto<Review> updateReview(
      @PathVariable @Parameter(description = "Review ID") String id,
      @Valid @RequestBody @Parameter(description = "Review update request") UpdateReviewRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Review review = reviewService.updateReview(id, request, userId);
    return ResponseDto.success("Review updated successfully", review);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a review", description = "Deletes a review by ID. Only the review author can delete it.", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteEndpoint
  public ResponseDto<Void> deleteReview(
      @PathVariable @Parameter(description = "Review ID") String id) {
    Long userId = SecurityUtils.getCurrentUserId();
    reviewService.deleteReview(id, userId);
    return ResponseDto.success("Review deleted successfully", null);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a review by ID", description = "Retrieves a single review by its ID. Public access.")
  @GetEnpoint
  public Review getReview(
      @PathVariable @Parameter(description = "Review ID") String id) {
    return reviewService.getReviewById(id);
  }

  @GetMapping("/post/{postId}")
  @Operation(summary = "Get reviews for a post", description = "Retrieves all reviews for a specific post. Public access.")
  @GetEnpoint
  public PageResponse<Review> getPostReviews(
      @PathVariable @Parameter(description = "Post ID") Long postId,
      @ParameterObject Pageable pageable) {
    return reviewService.getPostReviews(postId, pageable);

  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get reviews by user", description = "Retrieves all reviews created by a specific user")
  @GetEnpoint
  public PageResponse<Review> getUserReviews(
      @PathVariable @Parameter(description = "User ID") Long userId,
      @ParameterObject Pageable pageable) {
    return reviewService.getUserReviews(userId, pageable);
  }

  @GetMapping("/post/{postId}/stats")
  @Operation(summary = "Get review statistics for a post", description = "Returns average rating and review count for a post")
  @GetEnpoint
  public ResponseEntity<ResponseDto<java.util.Map<String, Object>>> getPostReviewStats(
      @PathVariable @Parameter(description = "Post ID") Long postId) {
    Double averageRating = reviewService.getAverageRating(postId);
    Long reviewCount = reviewService.getReviewCount(postId);

    java.util.Map<String, Object> stats = java.util.Map.of(
        "averageRating", averageRating,
        "reviewCount", reviewCount);

    return ResponseEntity.ok(ResponseDto.success(stats));
  }
}
