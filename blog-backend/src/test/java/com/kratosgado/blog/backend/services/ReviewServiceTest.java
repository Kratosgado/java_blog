package com.kratosgado.blog.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kratosgado.blog.backend.exceptions.ForbiddenException;
import com.kratosgado.blog.backend.exceptions.ResourceAlreadyExistsException;
import com.kratosgado.blog.backend.exceptions.ResourceNotFoundException;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private PostRepository postRepository;

  @InjectMocks
  private ReviewService reviewService;

  @Test
  @DisplayName("createReview should create and persist review when post exists and none by user")
  void createReview_shouldCreateReview() {
    CreateReviewRequest request = new CreateReviewRequest(1L, 5, "Great", "Content");
    User user = User.builder().id(10L).username("john").avatarUrl("avatar").email("e@e.com").build();

    when(postRepository.existsById(1L)).thenReturn(true);
    when(reviewRepository.existsByPostIdAndUserId(1L, 10L)).thenReturn(false);
    when(reviewRepository.save(any(Review.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Review result = reviewService.createReview(request, user);

    assertThat(result.getPostId()).isEqualTo(1L);
    assertThat(result.getUserId()).isEqualTo(10L);
    assertThat(result.getRating()).isEqualTo(5);
    assertThat(result.getAuthorName()).isEqualTo("john");
    assertThat(result.getAuthorAvatarUrl()).isEqualTo("avatar");
    assertThat(result.getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("createReview should throw when post does not exist")
  void createReview_whenPostMissing_shouldThrow() {
    CreateReviewRequest request = new CreateReviewRequest(1L, 5, "Great", "Content");
    User user = User.builder().id(10L).username("john").build();

    when(postRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> reviewService.createReview(request, user))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("createReview should prevent duplicate reviews by same user")
  void createReview_whenAlreadyExists_shouldThrow() {
    CreateReviewRequest request = new CreateReviewRequest(1L, 5, "Great", "Content");
    User user = User.builder().id(10L).username("john").build();

    when(postRepository.existsById(1L)).thenReturn(true);
    when(reviewRepository.existsByPostIdAndUserId(1L, 10L)).thenReturn(true);

    assertThatThrownBy(() -> reviewService.createReview(request, user))
        .isInstanceOf(ResourceAlreadyExistsException.class);
  }

  @Test
  @DisplayName("updateReview should apply non-null fields and save when owner matches")
  void updateReview_shouldUpdateOwnedReview() {
    Review existing = Review.builder()
        .id("r1")
        .postId(1L)
        .userId(10L)
        .rating(3)
        .title("Old")
        .content("Old content")
        .build();

    UpdateReviewRequest request = new UpdateReviewRequest(5, "New", "New content");

    when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));
    when(reviewRepository.save(any(Review.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Review result = reviewService.updateReview("r1", request, 10L);

    assertThat(result.getRating()).isEqualTo(5);
    assertThat(result.getTitle()).isEqualTo("New");
    assertThat(result.getContent()).isEqualTo("New content");
    assertThat(result.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("updateReview should allow partial updates when fields are null")
  void updateReview_withNullFields_shouldKeepExistingValues() {
    Review existing = Review.builder()
        .id("r1")
        .postId(1L)
        .userId(10L)
        .rating(3)
        .title("Old")
        .content("Old content")
        .build();

    UpdateReviewRequest request = new UpdateReviewRequest(null, null, "Only content");

    when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));
    when(reviewRepository.save(any(Review.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Review result = reviewService.updateReview("r1", request, 10L);

    assertThat(result.getRating()).isEqualTo(3);
    assertThat(result.getTitle()).isEqualTo("Old");
    assertThat(result.getContent()).isEqualTo("Only content");
  }

  @Test
  @DisplayName("updateReview should forbid updates by non owner")
  void updateReview_asOtherUser_shouldThrowForbidden() {
    Review existing = Review.builder().id("r1").postId(1L).userId(10L).build();
    UpdateReviewRequest request = new UpdateReviewRequest(5, "New", "New content");

    when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> reviewService.updateReview("r1", request, 20L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("updateReview should throw when review not found")
  void updateReview_whenMissing_shouldThrow() {
    UpdateReviewRequest request = new UpdateReviewRequest(5, "New", "New content");
    when(reviewRepository.findById("r1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.updateReview("r1", request, 10L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("deleteReview should delete when owner matches")
  void deleteReview_asOwner_shouldDelete() {
    Review existing = Review.builder().id("r1").postId(1L).userId(10L).build();
    when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

    reviewService.deleteReview("r1", 10L);

    verify(reviewRepository).delete(existing);
  }

  @Test
  @DisplayName("deleteReview should throw Forbidden when non owner")
  void deleteReview_asOtherUser_shouldThrowForbidden() {
    Review existing = Review.builder().id("r1").postId(1L).userId(10L).build();
    when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> reviewService.deleteReview("r1", 20L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("deleteReview should throw when review not found")
  void deleteReview_whenMissing_shouldThrow() {
    when(reviewRepository.findById("r1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.deleteReview("r1", 10L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("getReviewById should return review when exists")
  void getReviewById_shouldReturnReview() {
    Review existing = Review.builder().id("r1").postId(1L).userId(10L).build();
    when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

    Review result = reviewService.getReviewById("r1");

    assertThat(result).isSameAs(existing);
  }

  @Test
  @DisplayName("getReviewById should throw when not found")
  void getReviewById_whenMissing_shouldThrow() {
    when(reviewRepository.findById("r1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.getReviewById("r1"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("getPostReviews should use repository and map page response")
  void getPostReviews_shouldReturnPageResponse() {
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).build();
    Review review = Review.builder().id("r1").postId(1L).userId(10L).build();
    Page<Review> page = new PageImpl<>(List.of(review));

    when(reviewRepository.findByPostId(eq(1L), any(Pageable.class))).thenReturn(page);

    PageResponse<Review> response = reviewService.getPostReviews(1L, pageRequest);

    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.content()).containsExactly(review);
  }

  @Test
  @DisplayName("getUserReviews should return user reviews without user info")
  void getUserReviews_shouldReturnReviewWithoutUser() {
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).build();
    ReviewWithoutUser projection =
        org.mockito.Mockito.mock(ReviewWithoutUser.class);
    Page<ReviewWithoutUser> page = new PageImpl<>(List.of(projection));

    when(reviewRepository.findByUserId(eq(10L), any(Pageable.class))).thenReturn(page);

    PageResponse<ReviewWithoutUser> response = reviewService.getUserReviews(10L, pageRequest);

    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.content()).containsExactly(projection);
  }

  @Test
  @DisplayName("getAverageRating should return repository value or zero when null")
  void getAverageRating_shouldHandleNull() {
    when(reviewRepository.getAverageRating(1L)).thenReturn(4.5);

    assertThat(reviewService.getAverageRating(1L)).isEqualTo(4.5);

    when(reviewRepository.getAverageRating(2L)).thenReturn(null);

    assertThat(reviewService.getAverageRating(2L)).isEqualTo(0.0);
  }

  @Test
  @DisplayName("getReviewCount should delegate to repository")
  void getReviewCount_shouldReturnCount() {
    when(reviewRepository.countByPostId(1L)).thenReturn(3L);

    assertThat(reviewService.getReviewCount(1L)).isEqualTo(3L);
  }
}
