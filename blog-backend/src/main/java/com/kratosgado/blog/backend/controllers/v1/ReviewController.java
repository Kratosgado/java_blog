package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ReviewResponse.ReviewWithoutUser;
import com.kratosgado.blog.backend.models.Review;
import com.kratosgado.blog.backend.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {

  private final ReviewService reviewService;

  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @PostMapping
  @SecuredCreateEndpoint(summary = "Create a new review", description = "Creates a new review for a post. Requires authentication.")
  @ResponseStatus(HttpStatus.CREATED)
  public Review createReview(
      @Valid @RequestBody @Parameter(description = "Review creation request")
          CreateReviewRequest request,
      @AuthenticationPrincipal User user) {
    return reviewService.createReview(request, user);
  }

  @PutMapping("/{id}")
  @SecuredUpdateEndpoint(summary = "Update a review", description = "Updates an existing review. Only the review author can update it.")
  public Review updateReview(
      @PathVariable @Parameter(description = "Review ID") String id,
      @Valid @RequestBody @Parameter(description = "Review update request")
          UpdateReviewRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    return reviewService.updateReview(id, request, userId);
  }

  @DeleteMapping("/{id}")
  @DeleteEndpoint(summary = "Delete a review", description = "Deletes a review by ID. Only the review author can delete it.")
  public void deleteReview(@PathVariable @Parameter(description = "Review ID") String id) {
    Long userId = SecurityUtils.getCurrentUserId();
    reviewService.deleteReview(id, userId);
  }

  @GetMapping("/{id}")
  @GetEndpoint(summary = "Get a review by ID", description = "Retrieves a single review by its ID. Public access.")
  public Review getReview(@PathVariable @Parameter(description = "Review ID") String id) {
    return reviewService.getReviewById(id);
  }

  @GetMapping("/post/{postId}")
  @GetEndpoint(summary = "Get reviews for a post", description = "Retrieves all reviews for a specific post. Public access.")
  public PageResponse<Review> getPostReviews(
      @PathVariable @Parameter(description = "Post ID") Long postId,
      @ParameterObject PageRequest page) {
    return reviewService.getPostReviews(postId, page);
  }

  @GetMapping("/user/{userId}")
  @GetEndpoint(summary = "Get reviews by user", description = "Retrieves all reviews created by a specific user")
  public PageResponse<ReviewWithoutUser> getUserReviews(
      @PathVariable @Parameter(description = "User ID") Long userId,
      @ParameterObject PageRequest page) {
    return reviewService.getUserReviews(userId, page);
  }

  @GetMapping("/post/{postId}/stats")
  @GetEndpoint(summary = "Get review statistics for a post", description = "Returns average rating and review count for a post")
  public Map<String, Object> getPostReviewStats(
      @PathVariable @Parameter(description = "Post ID") Long postId) {
    Double averageRating = reviewService.getAverageRating(postId);
    Long reviewCount = reviewService.getReviewCount(postId);

    return java.util.Map.of(
        "averageRating", BlogUtils.round(averageRating), "reviewCount", reviewCount);
  }
}
