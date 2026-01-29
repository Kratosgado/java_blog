package com.kratosgado.blog.backend.controllers;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.DashboardService;
import com.kratosgado.blog.dtos.response.AnalyticsResponse;
import com.kratosgado.blog.dtos.response.EngagementStatsResponse;
import com.kratosgado.blog.dtos.response.PostDistributionResponse;
import com.kratosgado.blog.dtos.response.RecentActivityResponse;
import com.kratosgado.blog.dtos.response.StatCountResponse;
import com.kratosgado.blog.dtos.response.UserDashboardStatsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard and analytics APIs")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/stats")
  @Operation(summary = "Get dashboard statistics", description = "Retrieves overall statistics for the dashboard. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public StatCountResponse getStats() {
    return dashboardService.getDashboardStats();
  }

  @GetMapping("/user/stats")
  @Operation(summary = "Get user dashboard statistics", description = "Retrieves statistics for the current user's dashboard. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public UserDashboardStatsResponse getUserStats() {
    Long userId = SecurityUtils.getCurrentUserId();
    return dashboardService.getUserDashboardStats(userId);
  }

  @GetMapping("/analytics")
  @Operation(summary = "Get analytics data", description = "Retrieves analytics data for a date range. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public AnalyticsResponse getAnalytics(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "Start date") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "End date") LocalDateTime endDate) {

    if (startDate == null) {
      startDate = LocalDateTime.now().minusDays(30);
    }
    if (endDate == null) {
      endDate = LocalDateTime.now();
    }

    return dashboardService.getAnalytics(startDate, endDate);
  }

  @GetMapping("/posts/distribution")
  @Operation(summary = "Get post status distribution", description = "Retrieves distribution of posts by status. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public PostDistributionResponse getPostDistribution() {
    return dashboardService.getPostStatusDistribution();
  }

  @GetMapping("/engagement")
  @Operation(summary = "Get engagement statistics", description = "Retrieves engagement statistics like top posts and popular categories. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public EngagementStatsResponse getEngagement() {
    return dashboardService.getEngagementStats();
  }

  @GetMapping("/recent")
  @Operation(summary = "Get recent activity", description = "Retrieves recent activity like latest posts and comments. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public RecentActivityResponse getRecentActivity() {
    return dashboardService.getRecentActivity();
  }
}