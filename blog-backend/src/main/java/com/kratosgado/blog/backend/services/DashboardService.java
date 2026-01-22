package com.kratosgado.blog.backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.dao.PostDAO;
import com.kratosgado.blog.backend.dao.TagDAO;
import com.kratosgado.blog.backend.dao.UserDAO;
import com.kratosgado.blog.backend.dao.nosql.CommentMongoDAO;
import com.kratosgado.blog.backend.dao.nosql.ReviewMongoDAO;
import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;
import com.kratosgado.blog.models.Post;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final PostDAO postDAO;
  private final UserDAO userDAO;
  private final TagDAO tagDAO;
  private final CommentMongoDAO commentDAO;
  private final ReviewMongoDAO reviewDAO;

  public StatCountResponse getDashboardStats() {
    
    List<Post> allPosts = postDAO.getAllPosts();
    List<com.kratosgado.blog.models.User> allUsers = userDAO.getAllUsers();
    List<com.kratosgado.blog.models.Tag> allTags = tagDAO.getAllTags();
    long commentCount = commentDAO.getTotalCommentCount();
    long reviewCount = reviewDAO.getTotalReviewCount();

    StatCountResponse stats = new StatCountResponse(
        (long) allPosts.size(),
        (long) allUsers.size(),
        commentCount,
        (long) allTags.size(),
        reviewCount);

    return stats;
  }

  public UserDashboardStatsResponse getUserDashboardStats(Long userId) {

    List<Post> userPosts = postDAO.getPostsByUserId(userId.intValue());
    long userPostsCount = userPosts.size();
    
    long userCommentCount = commentDAO.getCommentsByUserId(userId).size();
    long userReviewCount = reviewDAO.getReviewsByUserId(userId).size();

    // Calculate total views across all user's posts
    long totalViews = userPosts.stream()
        .mapToLong(Post::getViews)
        .sum();

    return new UserDashboardStatsResponse(userPostsCount, userCommentCount, userReviewCount, totalViews);
  }

  public AnalyticsResponse getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {

    // Get all posts (in real implementation, filter by date)
    List<Post> posts = postDAO.getAllPosts();

    int totalPosts = posts.size();
    long totalViews = posts.stream().mapToLong(Post::getViews).sum();
    double averageViews = posts.isEmpty() ? 0 : posts.stream().mapToLong(Post::getViews).average().orElse(0);

    // Top posts by views
    List<AnalyticsResponse.TopPostData> topPosts = posts.stream()
        .sorted((p1, p2) -> Long.compare(p2.getViews(), p1.getViews()))
        .limit(10)
        .map(post -> new AnalyticsResponse.TopPostData(
            post.getId().longValue(),
            post.getTitle(),
            post.getViews(),
            post.getLikesCount()))
        .collect(Collectors.toList());

    return new AnalyticsResponse(totalPosts, totalViews, averageViews, topPosts);
  }

  public PostDistributionResponse getPostStatusDistribution() {

    List<Post> posts = postDAO.getAllPosts();
    long published = posts.stream().filter(p -> "PUBLISHED".equals(p.getStatus())).count();
    long draft = posts.stream().filter(p -> "DRAFT".equals(p.getStatus())).count();
    long privateCount = posts.stream().filter(p -> "PRIVATE".equals(p.getStatus())).count();

    return new PostDistributionResponse(published, draft, privateCount);
  }
}
