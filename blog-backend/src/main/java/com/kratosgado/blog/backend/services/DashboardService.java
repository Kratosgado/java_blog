package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.repositories.jdbc.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import com.kratosgado.blog.backend.repositories.jdbc.TagRepository;
import com.kratosgado.blog.backend.repositories.jdbc.UserRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.EngagementStatsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.RecentActivityResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final CategoryRepository categoryRepository;

  public DashboardService(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository,
      CommentRepository commentRepository, ReviewRepository reviewRepository,
      CategoryRepository categoryRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.tagRepository = tagRepository;
    this.commentRepository = commentRepository;
    this.reviewRepository = reviewRepository;
    this.categoryRepository = categoryRepository;
  }

  public StatCountResponse getDashboardStats() {
    return new StatCountResponse(
        postRepository.countAll(),
        userRepository.countAll(),
        commentRepository.countAll(),
        tagRepository.countAll(),
        reviewRepository.countAll());
  }

  public UserDashboardStatsResponse getUserDashboardStats(Long userId) {
    return new UserDashboardStatsResponse(
        postRepository.countPostsByUser(userId),
        commentRepository.countByUserId(userId),
        reviewRepository.countByUserId(userId),
        postRepository.sumViewsByUserId(userId));
  }

  public AnalyticsResponse getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
    // Basic implementation of analytics, focusing on top posts and general counts
    long totalViews = 0; // In a real app, this would be filtered by date range
    var topPosts = postRepository.findTopPostsByViews(10).stream()
        .map(p -> new AnalyticsResponse.TopPostData(p.getId(), p.getTitle(), p.getViews(), p.getLikesCount()))
        .collect(Collectors.toList());

    for (var post : topPosts) {
      totalViews += post.views();
    }

    int totalPosts = (int) postRepository.countPublishedPosts();
    double averageViews = totalPosts > 0 ? (double) totalViews / totalPosts : 0;

    return new AnalyticsResponse(totalPosts, totalViews, averageViews, topPosts);
  }

  public PostDistributionResponse getPostStatusDistribution() {
    return new PostDistributionResponse(
        postRepository.countByStatus(PostStatus.published),
        postRepository.countByStatus(PostStatus.draft),
        postRepository.countByStatus(PostStatus.private_post));
  }

  public EngagementStatsResponse getEngagementStats() {
    List<EngagementStatsResponse.PostEngagementSummary> topByViews = postRepository.findTopPostsByViews(5).stream()
        .map(p -> new EngagementStatsResponse.PostEngagementSummary(p.getId(), p.getTitle(), p.getSlug(), p.getViews(),
            p.getLikesCount()))
        .collect(Collectors.toList());

    // For now use the same as top by views as we don't have findTopPostsByLikes specifically
    List<EngagementStatsResponse.PostEngagementSummary> topByLikes = postRepository.findTopPostsByViews(5).stream()
        .map(p -> new EngagementStatsResponse.PostEngagementSummary(p.getId(), p.getTitle(), p.getSlug(), p.getViews(),
            p.getLikesCount()))
        .collect(Collectors.toList());

    List<EngagementStatsResponse.CategorySummaryWithCount> popularCategories = categoryRepository.findAllWithPostCount()
        .stream()
        .limit(5)
        .map(c -> new EngagementStatsResponse.CategorySummaryWithCount(c.id(), c.name(), c.slug(), c.postCount()))
        .collect(Collectors.toList());

    return new EngagementStatsResponse(topByViews, topByLikes, popularCategories);
  }

  public RecentActivityResponse getRecentActivity() {
    List<RecentActivityResponse.RecentPost> latestPosts = postRepository.findLatestPosts(5).stream()
        .map(p -> new RecentActivityResponse.RecentPost(p.getId(), p.getTitle(), p.getSlug(), "Author",
            p.getCreatedAt()))
        .collect(Collectors.toList());

    List<RecentActivityResponse.RecentComment> latestComments = commentRepository.findLatestComments(5).stream()
        .map(c -> new RecentActivityResponse.RecentComment(c.getId(), c.getPostId(), c.getAuthorName(), c.getContent(),
            c.getCreatedAt()))
        .collect(Collectors.toList());

    return new RecentActivityResponse(latestPosts, latestComments);
  }
}
