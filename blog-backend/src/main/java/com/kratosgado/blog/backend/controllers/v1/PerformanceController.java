package com.kratosgado.blog.backend.controllers.v1;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.utils.performance.PerformanceMonitor;
import com.kratosgado.blog.backend.utils.performance.PerformanceMonitor.OperationStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for monitoring application performance metrics.
 * Provides endpoints to view performance statistics for Repository and Service
 * operations.
 * 
 * <p>
 * Performance metrics are collected automatically via AOP aspects for all
 * repository
 * and service layer operations. Statistics include execution time (avg, median,
 * min, max)
 * and call counts.
 * 
 * <p>
 * Endpoints:
 * <ul>
 * <li>GET /api/performance/stats - Get all performance statistics as JSON</li>
 * <li>GET /api/performance/report - Get formatted text report</li>
 * <li>GET /api/performance/status - Get monitoring status</li>
 * <li>DELETE /api/performance/reset - Clear all metrics</li>
 * </ul>
 * 
 * @see PerformanceMonitor
 * @see PerformanceAspect
 */
@RestController
@RequestMapping("/performance")
@Tag(name = "Performance Monitoring", description = "Performance metrics and monitoring APIs")
public class PerformanceController {

  private static final Logger log = LoggerFactory.getLogger(PerformanceController.class);
  private final PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();

  /**
   * Get all performance statistics.
   */
  @GetMapping("/stats")
  @Operation(summary = "Get performance statistics", description = "Retrieves performance metrics for all tracked operations. Requires admin role.", security = @SecurityRequirement(name = "Bearer Authentication"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, OperationStats>> getStats() {
    log.info("Retrieving performance statistics");
    Map<String, OperationStats> stats = performanceMonitor.getAllStats();
    return ResponseEntity.ok(stats);
  }

  /**
   * Get formatted performance report.
   */
  @GetMapping("/report")
  @Operation(summary = "Get performance report", description = "Retrieves a formatted performance report as text. Requires admin role.", security = @SecurityRequirement(name = "Bearer Authentication"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> getReport() {
    log.info("Generating performance report");
    String report = performanceMonitor.getFormattedReport();
    return ResponseEntity.ok(report);
  }

  /**
   * Reset all performance metrics.
   */
  @DeleteMapping("/reset")
  @Operation(summary = "Reset performance metrics", description = "Clears all collected performance metrics. Requires admin role.", security = @SecurityRequirement(name = "Bearer Authentication"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> reset() {
    log.info("Resetting performance metrics");
    performanceMonitor.reset();
    return ResponseEntity.ok("Performance metrics reset successfully");
  }

  /**
   * Enable or disable performance monitoring.
   */
  @GetMapping("/status")
  @Operation(summary = "Get monitoring status", description = "Check if performance monitoring is enabled. Requires admin role.", security = @SecurityRequirement(name = "Bearer Authentication"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MonitoringStatus> getStatus() {
    boolean enabled = performanceMonitor.isEnabled();
    Map<String, OperationStats> stats = performanceMonitor.getAllStats();
    int totalOperations = stats.values().stream().mapToInt(s -> s.count).sum();

    return ResponseEntity.ok(new MonitoringStatus(enabled, stats.size(), totalOperations));
  }

  /**
   * Monitoring status response.
   */
  public record MonitoringStatus(boolean enabled, int trackedOperations, int totalCalls) {
  }
}
