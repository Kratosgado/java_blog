package com.kratosgado.blog.backend.controllers.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

  @Mock
  private ReviewService reviewService;

  @InjectMocks
  private ReviewController reviewController;

  @Test
  @DisplayName("createReview should delegate to service with authenticated user")
  void createReview_shouldUseAuthenticatedUser() {
    CreateReviewRequest request = new CreateReviewRequest(1L, 5, "Title", "Content");
    User user = User.builder().id(10L).username("john").build();
    Review review = Review.builder().id("r1").postId(1L).userId(10L).build();

    when(reviewService.createReview(request, user)).thenReturn(review);

    Review result = reviewController.createReview(request, user);

    assertThat(result).isSameAs(review);
  }

  @Test
  @DisplayName("updateReview should use SecurityUtils current user id")
  void updateReview_shouldUseCurrentUserId() {
    UpdateReviewRequest request = new UpdateReviewRequest(4, "T", "C");
    Review review = Review.builder().id("r1").postId(1L).userId(10L).build();

    when(reviewService.updateReview(eq("r1"), eq(request), eq(10L))).thenReturn(review);

    try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
      mocked.when(SecurityUtils::getCurrentUserId).thenReturn(10L);

      Review result = reviewController.updateReview("r1", request);

      assertThat(result).isSameAs(review);
      mocked.verify(SecurityUtils::getCurrentUserId);
    }
  }

  @Test
  @DisplayName("deleteReview should call service with current user id")
  void deleteReview_shouldUseCurrentUserId() {
    try (MockedStatic<SecurityUtils> mocked = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
      mocked.when(SecurityUtils::getCurrentUserId).thenReturn(10L);

      reviewController.deleteReview("r1");

      mocked.verify(SecurityUtils::getCurrentUserId);
    }
  }

  @Test
  @DisplayName("getReview should return single review")
  void getReview_shouldReturnReview() {
    Review review = Review.builder().id("r1").postId(1L).userId(10L).build();
    when(reviewService.getReviewById("r1")).thenReturn(review);

    Review result = reviewController.getReview("r1");

    assertThat(result).isSameAs(review);
  }

  @Test
  @DisplayName("getPostReviews should delegate to service with page request")
  void getPostReviews_shouldReturnPageResponse() {
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).build();
    Review review = Review.builder().id("r1").postId(1L).userId(10L).build();
    PageResponse<Review> page =
        new PageResponse<>(List.of(review), 0, 10, 1, 1, true, true);

    when(reviewService.getPostReviews(1L, pageRequest)).thenReturn(page);

    PageResponse<Review> result = reviewController.getPostReviews(1L, pageRequest);

    assertThat(result).isSameAs(page);
  }

  @Test
  @DisplayName("getUserReviews should return reviews without user info")
  void getUserReviews_shouldReturnPageResponse() {
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).build();
    ReviewWithoutUser projection = org.mockito.Mockito.mock(ReviewWithoutUser.class);
    PageResponse<ReviewWithoutUser> page =
        new PageResponse<>(List.of(projection), 0, 10, 1, 1, true, true);

    when(reviewService.getUserReviews(10L, pageRequest)).thenReturn(page);

    PageResponse<ReviewWithoutUser> result = reviewController.getUserReviews(10L, pageRequest);

    assertThat(result).isSameAs(page);
  }

  @Test
  @DisplayName("getPostReviewStats should return rounded average and count")
  void getPostReviewStats_shouldReturnStatsMap() {
    double avg = 4.456;
    long count = 3L;

    when(reviewService.getAverageRating(1L)).thenReturn(avg);
    when(reviewService.getReviewCount(1L)).thenReturn(count);

    Map<String, Object> result = reviewController.getPostReviewStats(1L);

    assertThat(result.get("averageRating"))
        .isEqualTo(BlogUtils.round(avg));
    assertThat(result.get("reviewCount")).isEqualTo(count);
  }
}
