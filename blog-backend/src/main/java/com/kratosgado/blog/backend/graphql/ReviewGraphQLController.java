package com.kratosgado.blog.backend.graphql;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;

@Controller
public class ReviewGraphQLController {

  private final ReviewService reviewService;
  private final UserService userService;
  private final PostService postService;

  public ReviewGraphQLController(ReviewService reviewService, UserService userService, PostService postService) {
    this.reviewService = reviewService;
    this.userService = userService;
    this.postService = postService;
  }

  @QueryMapping
  public Review review(@Argument String id) {
    return reviewService.getReviewById(id);
  }

  @QueryMapping
  public List<Review> reviewsByPost(@Argument Long postId) {
    // Return all reviews for a post
    return reviewService.getPostReviews(postId, PageRequest.of(0, 100)).getContent();
  }

  @MutationMapping
  public Review createReview(@Argument CreateReviewRequest input) {
    // This would need userId from authentication context
    // For now, using a placeholder userId
    Long userId = 1L; // TODO: Get from SecurityContext
    return reviewService.createReview(input, userId);
  }

  @MutationMapping
  public Review updateReview(@Argument String id, @Argument UpdateReviewRequest input) {
    // This would need userId from authentication context
    Long userId = 1L; // TODO: Get from SecurityContext
    return reviewService.updateReview(id, input, userId);
  }

  @MutationMapping
  public boolean deleteReview(@Argument String id) {
    // This would need userId from authentication context
    Long userId = 1L; // TODO: Get from SecurityContext
    reviewService.deleteReview(id, userId);
    return true;
  }

  // Field resolvers for Review type
  @SchemaMapping(typeName = "Review", field = "author")
  public User author(Review review) {
    return userService.getUserById(review.getUserId());
  }

  @SchemaMapping(typeName = "Review", field = "post")
  public Post post(Review review) {
    return postService.getPostById(review.getPostId());
  }
}
