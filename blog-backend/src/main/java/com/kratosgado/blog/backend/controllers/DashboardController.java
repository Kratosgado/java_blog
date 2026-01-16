package com.kratosgado.blog.backend.controllers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.DashboardService;
import com.kratosgado.blog.dtos.response.ResponseDto;

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
  public ResponseEntity<ResponseDto<Map<String, Object>>> getStats() {
    Map<String, Object> stats = dashboardService.getDashboardStats();
    return ResponseEntity.ok(ResponseDto.success(stats));
  }

  @GetMapping("/user/stats")
  @Operation(summary = "Get user dashboard statistics", description = "Retrieves statistics for the current user's dashboard. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public ResponseEntity<ResponseDto<Map<String, Object>>> getUserStats() {
    Long userId = SecurityUtils.getCurrentUserId();
    Map<String, Object> stats = dashboardService.getUserDashboardStats(userId);
    return ResponseEntity.ok(ResponseDto.success(stats));
  }

  @GetMapping("/analytics")
  @Operation(summary = "Get analytics data", description = "Retrieves analytics data for a date range. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public ResponseEntity<ResponseDto<Map<String, Object>>> getAnalytics(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
      @Parameter(description = "Start date") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
      @Parameter(description = "End date") LocalDateTime endDate) {
    
    if (startDate == null) {
      startDate = LocalDateTime.now().minusDays(30);
    }
    if (endDate == null) {
      endDate = LocalDateTime.now();
    }
    
    Map<String, Object> analytics = dashboardService.getAnalytics(startDate, endDate);
    return ResponseEntity.ok(ResponseDto.success(analytics));
  }

  @GetMapping("/posts/distribution")
  @Operation(summary = "Get post status distribution", description = "Retrieves distribution of posts by status. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetEnpoint
  public ResponseEntity<ResponseDto<Map<String, Long>>> getPostDistribution() {
    Map<String, Long> distribution = dashboardService.getPostStatusDistribution();
    return ResponseEntity.ok(ResponseDto.success(distribution));
  }
}
