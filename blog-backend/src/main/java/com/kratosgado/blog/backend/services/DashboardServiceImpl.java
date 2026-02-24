package com.kratosgado.blog.backend.services;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final CategoryRepository categoryRepository;
  private final Executor taskExecutor;

  public StatCountResponse getDashboardStats() {
    var postsCountFuture =
        CompletableFuture.supplyAsync(postRepository::count, taskExecutor);
    var usersFuture = CompletableFuture.supplyAsync(userRepository::count, taskExecutor);
    var commentsFuture =
        CompletableFuture.supplyAsync(commentRepository::count, taskExecutor);
    var tagsFuture = CompletableFuture.supplyAsync(tagRepository::count, taskExecutor);
    var reviewsFuture = CompletableFuture.supplyAsync(reviewRepository::count, taskExecutor);

    return CompletableFuture.allOf(
            postsCountFuture, usersFuture, commentsFuture, tagsFuture, reviewsFuture)
        .thenApply(
            v ->
                new StatCountResponse(
                    postsCountFuture.join(),
                    usersFuture.join(),
                    commentsFuture.join(),
                    tagsFuture.join(),
                    reviewsFuture.join()))
        .join();
  }

  public UserDashboardStatsResponse getUserDashboardStats(Long userId) {
    var postsFuture =
        CompletableFuture.supplyAsync(
            () -> postRepository.countByUserId(userId), taskExecutor);
    var commentsFuture =
        CompletableFuture.supplyAsync(
            () -> commentRepository.countByUserId(userId), taskExecutor);
    var reviewsFuture =
        CompletableFuture.supplyAsync(
            () -> reviewRepository.countByUserId(userId), taskExecutor);
    var viewsFuture =
        CompletableFuture.supplyAsync(
            () -> postRepository.sumViewsByUserId(userId), taskExecutor);

    return CompletableFuture.allOf(postsFuture, commentsFuture, reviewsFuture, viewsFuture)
        .thenApply(
            v ->
                new UserDashboardStatsResponse(
                    postsFuture.join(),
                    commentsFuture.join(),
                    reviewsFuture.join(),
                    viewsFuture.join()))
        .join();
  }

  public AnalyticsResponse getAnalytics(
      LocalDateTime startDate, LocalDateTime endDate) {
    var topPostsFuture =
        CompletableFuture.supplyAsync(
            () ->
                postRepository.findTopNByOrderByViewsDesc(10).stream()
                    .map(
                        p ->
                            new AnalyticsResponse.TopPostData(
                                p.getId(), p.getTitle(), p.getViews(), p.getLikesCount()))
                    .collect(Collectors.toList()),
            taskExecutor);
    var totalPostsFuture =
        CompletableFuture.supplyAsync(
            () -> (int) postRepository.countByStatus(PostStatus.published), taskExecutor);

    return CompletableFuture.allOf(topPostsFuture, totalPostsFuture)
        .thenApply(
            v -> {
              var topPosts = topPostsFuture.join();
              int totalPosts = totalPostsFuture.join();
              long totalViews =
                  topPosts.stream().mapToLong(AnalyticsResponse.TopPostData::views).sum();
              double averageViews = totalPosts > 0 ? (double) totalViews / totalPosts : 0;
              return new AnalyticsResponse(totalPosts, totalViews, averageViews, topPosts);
            })
        .join();
  }

  public PostDistributionResponse getPostStatusDistribution() {
    var publishedFuture =
        CompletableFuture.supplyAsync(
            () -> postRepository.countByStatus(PostStatus.published), taskExecutor);
    var draftFuture =
        CompletableFuture.supplyAsync(
            () -> postRepository.countByStatus(PostStatus.draft), taskExecutor);
    var privateFuture =
        CompletableFuture.supplyAsync(
            () -> postRepository.countByStatus(PostStatus.private_post), taskExecutor);

    return CompletableFuture.allOf(publishedFuture, draftFuture, privateFuture)
        .thenApply(
            v ->
                new PostDistributionResponse(
                    publishedFuture.join(), draftFuture.join(), privateFuture.join()))
        .join();
  }

  public EngagementStatsResponse getEngagementStats() {
    var topByViewsFuture =
        CompletableFuture.supplyAsync(
            () ->
                postRepository.findTopNByOrderByViewsDesc(5).stream()
                    .map(
                        p ->
                            new EngagementStatsResponse.PostEngagementSummary(
                                p.getId(),
                                p.getTitle(),
                                p.getSlug(),
                                p.getViews(),
                                p.getLikesCount()))
                    .collect(Collectors.toList()),
            taskExecutor);

    var topByLikesFuture =
        CompletableFuture.supplyAsync(
            () ->
                postRepository
                    .findTopNByOrderByViewsDesc(5)
                    .stream() // Note: Should probably be OrderByLikesDesc, but keeping original
                    // logic
                    .map(
                        p ->
                            new EngagementStatsResponse.PostEngagementSummary(
                                p.getId(),
                                p.getTitle(),
                                p.getSlug(),
                                p.getViews(),
                                p.getLikesCount()))
                    .collect(Collectors.toList()),
            taskExecutor);

    var popularCategoriesFuture =
        CompletableFuture.supplyAsync(
            () ->
                categoryRepository.findAllWithPostCount().stream()
                    .limit(5)
                    .map(
                        c ->
                            new EngagementStatsResponse.CategorySummaryWithCount(
                                c.id(), c.name(), c.slug(), c.postCount().intValue()))
                    .collect(Collectors.toList()),
            taskExecutor);

    return CompletableFuture.allOf(topByViewsFuture, topByLikesFuture, popularCategoriesFuture)
        .thenApply(
            v ->
                new EngagementStatsResponse(
                    topByViewsFuture.join(),
                    topByLikesFuture.join(),
                    popularCategoriesFuture.join()))
        .join();
  }

  public RecentActivityResponse getRecentActivity() {
    var latestPostsFuture =
        CompletableFuture.supplyAsync(
            () ->
                postRepository.findTopNByOrderByCreatedAtDesc(5).stream()
                    .map(
                        p ->
                            new RecentActivityResponse.RecentPost(
                                p.getId(), p.getTitle(), p.getSlug(), p.getCreatedAt()))
                    .collect(Collectors.toList()),
            taskExecutor);

    var latestCommentsFuture =
        CompletableFuture.supplyAsync(
            () ->
                commentRepository
                    .findTopNByOrderByCreatedAtDesc(
                        org.springframework.data.domain.PageRequest.of(0, 5))
                    .stream()
                    .map(
                        c ->
                            new RecentActivityResponse.RecentComment(
                                c.getId(),
                                c.getPostId(),
                                c.getAuthorName(),
                                c.getContent(),
                                c.getCreatedAt()))
                    .collect(Collectors.toList()),
            taskExecutor);

    return CompletableFuture.allOf(latestPostsFuture, latestCommentsFuture)
        .thenApply(
            v -> new RecentActivityResponse(latestPostsFuture.join(), latestCommentsFuture.join()))
        .join();
  }
}
