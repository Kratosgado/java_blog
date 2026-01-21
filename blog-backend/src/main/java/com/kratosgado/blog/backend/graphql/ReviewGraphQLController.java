package com.kratosgado.blog.backend.graphql;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.CreateReviewRequest;
import com.kratosgado.blog.dtos.request.UpdateReviewRequest;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.dtos.response.ReviewResponse;
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
  public ReviewResponse review(@Argument String id) {
    return reviewService.getReviewById(id);
  }

  @QueryMapping
  public List<ReviewResponse> reviewsByPost(@Argument Long postId) {
    return reviewService.getPostReviews(postId, PageRequest.of(0, 100)).getContent();
  }

  @MutationMapping
  public ReviewResponse createReview(@Argument CreateReviewRequest input) {
    return reviewService.createReview(input, SecurityUtils.getCurrentUserId());
  }

  @MutationMapping
  public ReviewResponse updateReview(@Argument String id, @Argument UpdateReviewRequest input) {
    return reviewService.updateReview(id, input, SecurityUtils.getCurrentUserId());
  }

  @MutationMapping
  public boolean deleteReview(@Argument String id) {
    reviewService.deleteReview(id, SecurityUtils.getCurrentUserId());
    return true;
  }

  // Field resolvers for ReviewResponse type
  @SchemaMapping(typeName = "Review", field = "author")
  public User author(ReviewResponse review) {
    return userService.getUserById(review.author().id());
  }

  @SchemaMapping(typeName = "Review", field = "post")
  public PostResponse post(ReviewResponse review) {
    return postService.getPostById(review.postId());
  }
}
