package com.kratosgado.blog.backend.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.repositories.jpa.CommentRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.ReviewRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.models.Post;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final ReviewRepository reviewRepository;

  public Map<String, Object> getDashboardStats() {

    Map<String, Object> stats = new HashMap<>();

    stats.put("totalPosts", postRepository.count());
    stats.put("totalUsers", userRepository.count());
    stats.put("totalComments", commentRepository.count());
    stats.put("totalTags", tagRepository.count());
    stats.put("totalReviews", reviewRepository.count());

    return stats;
  }

  public Map<String, Object> getUserDashboardStats(Long userId) {

    Map<String, Object> stats = new HashMap<>();

    long userPosts = postRepository.count();
    long userComments = commentRepository.count();
    long userReviews = reviewRepository.count();

    stats.put("totalPosts", userPosts);
    stats.put("totalComments", userComments);
    stats.put("totalReviews", userReviews);

    // Calculate total views across all user's posts
    List<Post> userPostsList = postRepository.findAll();
    long totalViews = userPostsList.stream()
        .mapToLong(Post::getViews)
        .sum();

    stats.put("totalViews", totalViews);

    return stats;
  }

  public Map<String, Object> getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {

    Map<String, Object> analytics = new HashMap<>();

    // Get all posts (in real implementation, filter by date)
    List<Post> posts = postRepository.findAll();

    analytics.put("totalPosts", posts.size());
    analytics.put("totalViews", posts.stream().mapToLong(Post::getViews).sum());
    analytics.put("averageViews", posts.isEmpty() ? 0 : posts.stream().mapToLong(Post::getViews).average().orElse(0));

    // Top posts by views
    List<Map<String, Object>> topPosts = posts.stream()
        .sorted((p1, p2) -> Long.compare(p2.getViews(), p1.getViews()))
        .limit(10)
        .map(post -> {
          Map<String, Object> postData = new HashMap<>();
          postData.put("id", post.getId());
          postData.put("title", post.getTitle());
          postData.put("views", post.getViews());
          postData.put("likesCount", post.getLikesCount());
          return postData;
        })
        .collect(Collectors.toList());

    analytics.put("topPosts", topPosts);

    return analytics;
  }

  public Map<String, Long> getPostStatusDistribution() {

    Map<String, Long> distribution = new HashMap<>();

    List<Post> posts = postRepository.findAll();
    distribution.put("PUBLISHED", posts.stream().filter(p -> "PUBLISHED".equals(p.getStatus())).count());
    distribution.put("DRAFT", posts.stream().filter(p -> "DRAFT".equals(p.getStatus())).count());
    distribution.put("PRIVATE", posts.stream().filter(p -> "PRIVATE".equals(p.getStatus())).count());

    return distribution;
  }
}
