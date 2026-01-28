package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import com.kratosgado.blog.backend.repositories.jdbc.TagRepository;
import com.kratosgado.blog.backend.repositories.jdbc.UserRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.mongo.ReviewRepository;
import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DashboardService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;

  public DashboardService(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository, CommentRepository commentRepository, ReviewRepository reviewRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.tagRepository = tagRepository;
    this.commentRepository = commentRepository;
    this.reviewRepository = reviewRepository;
  }

  public StatCountResponse getDashboardStats() {
    // TODO: Implement this method
    return null;
  }

  public UserDashboardStatsResponse getUserDashboardStats(Long userId) {
    // TODO: Implement this method
    return null;
  }

  public AnalyticsResponse getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
    // TODO: Implement this method
    return null;
  }

  public PostDistributionResponse getPostStatusDistribution() {
    // TODO: Implement this method
    return null;
  }
}
