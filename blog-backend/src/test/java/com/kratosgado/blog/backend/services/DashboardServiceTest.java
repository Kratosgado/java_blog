package com.kratosgado.blog.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.EngagementStatsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.RecentActivityResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;
import com.kratosgado.blog.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private DashboardService dashboardService;

  private com.kratosgado.blog.dtos.response.PostResponse.PostView postView1;
  private com.kratosgado.blog.dtos.response.PostResponse.PostView postView2;

  @BeforeEach
  void setUp() {
    postView1 = org.mockito.Mockito.mock(com.kratosgado.blog.dtos.response.PostResponse.PostView.class, org.mockito.Mockito.withSettings().lenient());
    org.mockito.Mockito.lenient().when(postView1.getId()).thenReturn(1L);
    org.mockito.Mockito.lenient().when(postView1.getTitle()).thenReturn("Post 1");
    org.mockito.Mockito.lenient().when(postView1.getViews()).thenReturn(100);
    org.mockito.Mockito.lenient().when(postView1.getLikesCount()).thenReturn(10);

    postView2 = org.mockito.Mockito.mock(com.kratosgado.blog.dtos.response.PostResponse.PostView.class, org.mockito.Mockito.withSettings().lenient());
    org.mockito.Mockito.lenient().when(postView2.getId()).thenReturn(2L);
    org.mockito.Mockito.lenient().when(postView2.getTitle()).thenReturn("Post 2");
    org.mockito.Mockito.lenient().when(postView2.getViews()).thenReturn(50);
    org.mockito.Mockito.lenient().when(postView2.getLikesCount()).thenReturn(5);
  }

  @Test
  @DisplayName("getDashboardStats should aggregate counts from repositories")
  void getDashboardStats_shouldReturnAggregatedCounts() {
    when(postRepository.count()).thenReturn(10L);
    when(userRepository.count()).thenReturn(5L);
    when(commentRepository.count()).thenReturn(20L);
    when(tagRepository.count()).thenReturn(3L);
    when(reviewRepository.count()).thenReturn(7L);

    StatCountResponse response = dashboardService.getDashboardStats();

    assertThat(response.totalPosts()).isEqualTo(10L);
    assertThat(response.totalUsers()).isEqualTo(5L);
    assertThat(response.totalComments()).isEqualTo(20L);
    assertThat(response.totalTags()).isEqualTo(3L);
    assertThat(response.totalReviews()).isEqualTo(7L);
  }

  @Test
  @DisplayName("getUserDashboardStats should use user-scoped repository methods")
  void getUserDashboardStats_shouldReturnUserStats() {
    Long userId = 42L;
    when(postRepository.countByUserId(userId)).thenReturn(4L);
    when(commentRepository.countByUserId(userId)).thenReturn(9L);
    when(reviewRepository.countByUserId(userId)).thenReturn(2L);
    when(postRepository.sumViewsByUserId(userId)).thenReturn(123L);

    UserDashboardStatsResponse response = dashboardService.getUserDashboardStats(userId);

    assertThat(response.totalPosts()).isEqualTo(4L);
    assertThat(response.totalComments()).isEqualTo(9L);
    assertThat(response.totalReviews()).isEqualTo(2L);
    assertThat(response.totalViews()).isEqualTo(123L);
  }

  @Test
  @DisplayName("getAnalytics should compute totals and average views")
  void getAnalytics_shouldComputeTotalsAndAverage() {
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now();

    when(postRepository.findTopNByOrderByViewsDesc(10))
        .thenReturn(List.of(postView1, postView2));
    when(postRepository.countByStatus(PostStatus.published)).thenReturn(2L);

    AnalyticsResponse response = dashboardService.getAnalytics(start, end);

    assertThat(response.totalPosts()).isEqualTo(2);
    assertThat(response.totalViews()).isEqualTo(150L);
    assertThat(response.averageViews()).isEqualTo(75.0);
    assertThat(response.topPosts()).hasSize(2);
  }

  @Test
  @DisplayName("getAnalytics should handle zero published posts")
  void getAnalytics_whenNoPublishedPosts_shouldReturnZeroAverage() {
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now();

    when(postRepository.findTopNByOrderByViewsDesc(10))
        .thenReturn(List.of(postView1));
    when(postRepository.countByStatus(PostStatus.published)).thenReturn(0L);

    AnalyticsResponse response = dashboardService.getAnalytics(start, end);

    assertThat(response.totalPosts()).isEqualTo(0);
    assertThat(response.averageViews()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("getPostStatusDistribution should return counts per status")
  void getPostStatusDistribution_shouldReturnCounts() {
    when(postRepository.countByStatus(PostStatus.published)).thenReturn(5L);
    when(postRepository.countByStatus(PostStatus.draft)).thenReturn(3L);
    when(postRepository.countByStatus(PostStatus.private_post)).thenReturn(1L);

    PostDistributionResponse response = dashboardService.getPostStatusDistribution();

    assertThat(response.published()).isEqualTo(5L);
    assertThat(response.draft()).isEqualTo(3L);
    assertThat(response.privateCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("getEngagementStats should map post and category projections correctly")
  void getEngagementStats_shouldReturnEngagementSummaries() {
    when(postRepository.findTopNByOrderByViewsDesc(5))
        .thenReturn(List.of(postView1, postView2));

    com.kratosgado.blog.dtos.response.CategoryResponse categoryProjection =
        new com.kratosgado.blog.dtos.response.CategoryResponse(1L, "Tech", "tech", "desc", 3L);

    when(categoryRepository.findAllWithPostCount())
        .thenReturn(List.of(categoryProjection));

    EngagementStatsResponse response = dashboardService.getEngagementStats();

    assertThat(response.topPostsByViews()).hasSize(2);
    assertThat(response.topPostsByLikes()).hasSize(2);
    assertThat(response.popularCategories()).hasSize(1);
    assertThat(response.popularCategories().get(0).postCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("getRecentActivity should return latest posts and comments")
  void getRecentActivity_shouldReturnRecentPostsAndComments() {
    com.kratosgado.blog.dtos.response.PostResponse.PostView recentPost =
        org.mockito.Mockito.mock(com.kratosgado.blog.dtos.response.PostResponse.PostView.class);
    org.mockito.Mockito.when(recentPost.getId()).thenReturn(1L);
    org.mockito.Mockito.when(recentPost.getTitle()).thenReturn("Recent Post");
    org.mockito.Mockito.when(recentPost.getSlug()).thenReturn("recent-post");
    org.mockito.Mockito.when(recentPost.getCreatedAt())
        .thenReturn(LocalDateTime.of(2024, 1, 1, 10, 0));

    com.kratosgado.blog.models.Comment recentComment = new com.kratosgado.blog.models.Comment();
    recentComment.setId("c1");
    recentComment.setPostId(1L);
    recentComment.setAuthorName("johndoe");
    recentComment.setContent("Nice post");
    recentComment.setCreatedAt(LocalDateTime.of(2024, 1, 2, 12, 0));

    when(postRepository.findTopNByOrderByCreatedAtDesc(5))
        .thenReturn(List.of(recentPost));
    when(commentRepository.findTopNByOrderByCreatedAtDesc(5))
        .thenReturn(List.of(recentComment));

    RecentActivityResponse response = dashboardService.getRecentActivity();

    assertThat(response.latestPosts()).hasSize(1);
    assertThat(response.latestComments()).hasSize(1);
    assertThat(response.latestComments().get(0).authorName()).isEqualTo("johndoe");
  }
}
