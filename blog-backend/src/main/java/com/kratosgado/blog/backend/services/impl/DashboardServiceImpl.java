package com.kratosgado.blog.backend.services.impl;

import com.kratosgado.blog.backend.services.*;


import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.backend.utils.BlogConstants.CacheNames;
import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.EngagementStatsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.RecentActivityResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;
import com.kratosgado.blog.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class DashboardServiceImpl implements DashboardService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final CategoryRepository categoryRepository;

  @Cacheable(value = CacheNames.DASHBOARDSTATS, key = "'dashboard-stats'")
  public StatCountResponse getDashboardStats() {
    return new StatCountResponse(
        postRepository.count(),
        userRepository.count(),
        commentRepository.count(),
        tagRepository.count(),
        reviewRepository.count());
  }

  @Cacheable(value = CacheNames.DASHBOARDSTATS, key = "'user-dashboard-stats'")
  public UserDashboardStatsResponse getUserDashboardStats(Long userId) {
    return new UserDashboardStatsResponse(
        postRepository.countByUserId(userId),
        commentRepository.countByUserId(userId),
        reviewRepository.countByUserId(userId),
        postRepository.sumViewsByUserId(userId));
  }

  @Cacheable(value = CacheNames.DASHBOARDSTATS, key = "'analytics'")
  public AnalyticsResponse getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
    long totalViews = 0;
    var topPosts =
        postRepository.findTopNByOrderByViewsDesc(10).stream()
            .map(
                p ->
                    new AnalyticsResponse.TopPostData(
                        p.getId(), p.getTitle(), p.getViews(), p.getLikesCount()))
            .collect(Collectors.toList());

    for (var post : topPosts) {
      totalViews += post.views();
    }

    int totalPosts = (int) postRepository.countByStatus(PostStatus.published);
    double averageViews = totalPosts > 0 ? (double) totalViews / totalPosts : 0;

    return new AnalyticsResponse(totalPosts, totalViews, averageViews, topPosts);
  }

  @Cacheable(value = CacheNames.DASHBOARDSTATS, key = "'post-status-distribution'")
  public PostDistributionResponse getPostStatusDistribution() {
    return new PostDistributionResponse(
        postRepository.countByStatus(PostStatus.published),
        postRepository.countByStatus(PostStatus.draft),
        postRepository.countByStatus(PostStatus.private_post));
  }

  @Cacheable(value = CacheNames.DASHBOARDSTATS, key = "'engagement-stats'")
  public EngagementStatsResponse getEngagementStats() {
    List<EngagementStatsResponse.PostEngagementSummary> topByViews =
        postRepository.findTopNByOrderByViewsDesc(5).stream()
            .map(
                p ->
                    new EngagementStatsResponse.PostEngagementSummary(
                        p.getId(), p.getTitle(), p.getSlug(), p.getViews(), p.getLikesCount()))
            .collect(Collectors.toList());

    List<EngagementStatsResponse.PostEngagementSummary> topByLikes =
        postRepository.findTopNByOrderByViewsDesc(5).stream()
            .map(
                p ->
                    new EngagementStatsResponse.PostEngagementSummary(
                        p.getId(), p.getTitle(), p.getSlug(), p.getViews(), p.getLikesCount()))
            .collect(Collectors.toList());

    List<EngagementStatsResponse.CategorySummaryWithCount> popularCategories =
        categoryRepository.findAllWithPostCount().stream()
            .limit(5)
            .map(
                c ->
                    new EngagementStatsResponse.CategorySummaryWithCount(
                        c.id(), c.name(), c.slug(), c.postCount().intValue()))
            .collect(Collectors.toList());

    return new EngagementStatsResponse(topByViews, topByLikes, popularCategories);
  }

  @Cacheable(value = CacheNames.DASHBOARDSTATS, key = "'recent-activity'")
  public RecentActivityResponse getRecentActivity() {
    List<RecentActivityResponse.RecentPost> latestPosts =
        postRepository.findTopNByOrderByCreatedAtDesc(5).stream()
            .map(
                p ->
                    new RecentActivityResponse.RecentPost(
                        p.getId(), p.getTitle(), p.getSlug(), p.getCreatedAt()))
            .collect(Collectors.toList());

    var page = PageRequest.of(0, 5);
    List<RecentActivityResponse.RecentComment> latestComments =
        commentRepository.findTopNByOrderByCreatedAtDesc(page).stream()
            .map(
                c ->
                    new RecentActivityResponse.RecentComment(
                        c.getId(),
                        c.getPostId(),
                        c.getAuthorName(),
                        c.getContent(),
                        c.getCreatedAt()))
            .collect(Collectors.toList());

    return new RecentActivityResponse(latestPosts, latestComments);
  }
}
