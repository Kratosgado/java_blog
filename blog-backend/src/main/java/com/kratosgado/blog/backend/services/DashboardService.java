package com.kratosgado.blog.backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;
import com.kratosgado.blog.models.Post;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;

  public StatCountResponse getDashboardStats() {
    
    long postCount = postRepository.count();
    long userCount = userRepository.count();
    long tagCount = tagRepository.count();
    long commentCount = commentRepository.count();
    long reviewCount = reviewRepository.count();

    StatCountResponse stats = new StatCountResponse(
        postCount,
        userCount,
        commentCount,
        tagCount,
        reviewCount);

    return stats;
  }

  public UserDashboardStatsResponse getUserDashboardStats(Long userId) {

    Page<Post> userPostsPage = postRepository.findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE));
    long userPostsCount = userPostsPage.getTotalElements();
    
    long userCommentCount = commentRepository.count();
    long userReviewCount = reviewRepository.count();

    // Calculate total views across all user's posts
    long totalViews = userPostsPage.getContent().stream()
        .mapToLong(Post::getViews)
        .sum();

    return new UserDashboardStatsResponse(userPostsCount, userCommentCount, userReviewCount, totalViews);
  }

  public AnalyticsResponse getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {

    // Get all posts (in real implementation, filter by date)
    Page<Post> postsPage = postRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE));
    List<Post> posts = postsPage.getContent();

    int totalPosts = posts.size();
    long totalViews = posts.stream().mapToLong(Post::getViews).sum();
    double averageViews = posts.isEmpty() ? 0 : posts.stream().mapToLong(Post::getViews).average().orElse(0);

    // Top posts by views
    List<AnalyticsResponse.TopPostData> topPosts = posts.stream()
        .sorted((p1, p2) -> Long.compare(p2.getViews(), p1.getViews()))
        .limit(10)
        .map(post -> new AnalyticsResponse.TopPostData(
            post.getId(),
            post.getTitle(),
            post.getViews(),
            post.getLikesCount()))
        .collect(Collectors.toList());

    return new AnalyticsResponse(totalPosts, totalViews, averageViews, topPosts);
  }

  public PostDistributionResponse getPostStatusDistribution() {

    Page<Post> postsPage = postRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE));
    List<Post> posts = postsPage.getContent();
    
    long published = posts.stream().filter(p -> "PUBLISHED".equals(p.getStatus().name())).count();
    long draft = posts.stream().filter(p -> "DRAFT".equals(p.getStatus().name())).count();
    long privateCount = posts.stream().filter(p -> "PRIVATE".equals(p.getStatus().name())).count();

    return new PostDistributionResponse(published, draft, privateCount);
  }
}
