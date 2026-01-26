package com.kratosgado.blog.backend.graphql;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.dtos.response.ReviewResponse.ReviewWithoutUser;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.User;

@Controller
public class ReviewGraphQLController {

  private final ReviewService reviewService;
  private final PostService postService;

  public ReviewGraphQLController(ReviewService reviewService, PostService postService) {
    this.reviewService = reviewService;
    this.postService = postService;
  }

  @QueryMapping
  public Review review(@Argument String id) {
    return reviewService.getReviewById(id);
  }

  @QueryMapping
  public PageResponse<Review> reviewsByPost(
      @Argument Long postId,
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return reviewService.getPostReviews(postId, pageable);
  }

  @QueryMapping
  public PageResponse<ReviewWithoutUser> reviewsByUser(
      @Argument Long userId,
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return reviewService.getUserReviews(userId, pageable);
  }

  @MutationMapping
  public Review createReview(@Argument CreateReviewRequest input, @AuthenticationPrincipal User user) {
    return reviewService.createReview(input, Long.valueOf(user.getId()));
  }

  @MutationMapping
  public Review updateReview(@Argument String id, @Argument UpdateReviewRequest input) {
    return reviewService.updateReview(id, input, SecurityUtils.getCurrentUserId());
  }

  @MutationMapping
  public boolean deleteReview(@Argument String id) {
    reviewService.deleteReview(id, SecurityUtils.getCurrentUserId());
    return true;
  }

  @SchemaMapping(typeName = "Review", field = "post")
  public PostResponse post(Review review) {
    return postService.getPostById(review.getPostId());
  }
}
