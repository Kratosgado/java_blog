package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.EngagementStatsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.RecentActivityResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;
import java.time.LocalDateTime;

public interface DashboardService {

  StatCountResponse getDashboardStats();

  UserDashboardStatsResponse getUserDashboardStats(Long userId);

  AnalyticsResponse getAnalytics(LocalDateTime startDate, LocalDateTime endDate);

  PostDistributionResponse getPostStatusDistribution();

  EngagementStatsResponse getEngagementStats();

  RecentActivityResponse getRecentActivity();
}
