package com.kratosgado.blog.utils.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performance monitoring utility for measuring operation execution times.
 * Provides timing, statistics, and performance tracking capabilities.
 */
public class PerformanceMonitor {
  private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
  private static final PerformanceMonitor instance = new PerformanceMonitor();
  
  private final Map<String, List<Long>> operationTimes = new ConcurrentHashMap<>();
  private final Map<String, Long> slowQueryThresholds = new ConcurrentHashMap<>();
  private boolean enabled = true;
  
  // Default slow query threshold: 100ms
  private static final long DEFAULT_SLOW_THRESHOLD_MS = 100;
  
  private PerformanceMonitor() {
    // Private constructor for singleton
  }
  
  public static PerformanceMonitor getInstance() {
    return instance;
  }
  
  /**
   * Measure execution time of a task and return its result.
   */
  public <T> T measure(String operation, Supplier<T> task) {
    if (!enabled) {
      return task.get();
    }
    
    long startTime = System.nanoTime();
    try {
      return task.get();
    } finally {
      long endTime = System.nanoTime();
      long durationMs = (endTime - startTime) / 1_000_000;
      recordMetric(operation, durationMs);
    }
  }
  
  /**
   * Measure execution time of a runnable task.
   */
  public void measureVoid(String operation, Runnable task) {
    if (!enabled) {
      task.run();
      return;
    }
    
    long startTime = System.nanoTime();
    try {
      task.run();
    } finally {
      long endTime = System.nanoTime();
      long durationMs = (endTime - startTime) / 1_000_000;
      recordMetric(operation, durationMs);
    }
  }
  
  /**
   * Record a performance metric.
   */
  private void recordMetric(String operation, long durationMs) {
    operationTimes.computeIfAbsent(operation, k -> new ArrayList<>()).add(durationMs);
    
    // Check for slow queries
    long threshold = slowQueryThresholds.getOrDefault(operation, DEFAULT_SLOW_THRESHOLD_MS);
    if (durationMs > threshold) {
      logger.warn("Slow operation detected: {} took {}ms (threshold: {}ms)", 
        operation, durationMs, threshold);
    } else {
      logger.debug("{} completed in {}ms", operation, durationMs);
    }
  }
  
  /**
   * Set custom slow query threshold for specific operation.
   */
  public void setSlowThreshold(String operation, long thresholdMs) {
    slowQueryThresholds.put(operation, thresholdMs);
  }
  
  /**
   * Get statistics for a specific operation.
   */
  public OperationStats getStats(String operation) {
    List<Long> times = operationTimes.get(operation);
    if (times == null || times.isEmpty()) {
      return new OperationStats(operation, 0, 0, 0, 0, 0);
    }
    
    long sum = 0;
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    
    for (long time : times) {
      sum += time;
      min = Math.min(min, time);
      max = Math.max(max, time);
    }
    
    int count = times.size();
    double average = (double) sum / count;
    
    // Calculate median
    List<Long> sortedTimes = new ArrayList<>(times);
    sortedTimes.sort(Long::compareTo);
    long median = sortedTimes.get(sortedTimes.size() / 2);
    
    return new OperationStats(operation, count, average, median, min, max);
  }
  
  /**
   * Get all operation statistics.
   */
  public Map<String, OperationStats> getAllStats() {
    Map<String, OperationStats> allStats = new HashMap<>();
    for (String operation : operationTimes.keySet()) {
      allStats.put(operation, getStats(operation));
    }
    return allStats;
  }
  
  /**
   * Print performance report to logger.
   */
  public void printReport() {
    logger.info("=== Performance Report ===");
    Map<String, OperationStats> stats = getAllStats();
    
    if (stats.isEmpty()) {
      logger.info("No performance data collected");
      return;
    }
    
    for (Map.Entry<String, OperationStats> entry : stats.entrySet()) {
      OperationStats stat = entry.getValue();
      logger.info("Operation: {}", stat.operation);
      logger.info("  Count: {}", stat.count);
      logger.info("  Average: {:.2f}ms", stat.average);
      logger.info("  Median: {}ms", stat.median);
      logger.info("  Min: {}ms", stat.min);
      logger.info("  Max: {}ms", stat.max);
    }
  }
  
  /**
   * Get formatted performance report as string.
   */
  public String getFormattedReport() {
    StringBuilder report = new StringBuilder();
    report.append("=== Performance Report ===\n");
    Map<String, OperationStats> stats = getAllStats();
    
    if (stats.isEmpty()) {
      report.append("No performance data collected\n");
      return report.toString();
    }
    
    for (Map.Entry<String, OperationStats> entry : stats.entrySet()) {
      OperationStats stat = entry.getValue();
      report.append(String.format("Operation: %s\n", stat.operation));
      report.append(String.format("  Count: %d\n", stat.count));
      report.append(String.format("  Average: %.2fms\n", stat.average));
      report.append(String.format("  Median: %dms\n", stat.median));
      report.append(String.format("  Min: %dms\n", stat.min));
      report.append(String.format("  Max: %dms\n", stat.max));
      report.append("\n");
    }
    
    return report.toString();
  }
  
  /**
   * Clear all recorded metrics.
   */
  public void reset() {
    operationTimes.clear();
    logger.info("Performance metrics reset");
  }
  
  /**
   * Enable or disable performance monitoring.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    logger.info("Performance monitoring {}", enabled ? "enabled" : "disabled");
  }
  
  /**
   * Check if monitoring is enabled.
   */
  public boolean isEnabled() {
    return enabled;
  }
  
  /**
   * Operation statistics data class.
   */
  public static class OperationStats {
    public final String operation;
    public final int count;
    public final double average;
    public final long median;
    public final long min;
    public final long max;
    
    public OperationStats(String operation, int count, double average, long median, long min, long max) {
      this.operation = operation;
      this.count = count;
      this.average = average;
      this.median = median;
      this.min = min;
      this.max = max;
    }
    
    @Override
    public String toString() {
      return String.format("OperationStats{operation='%s', count=%d, avg=%.2fms, median=%dms, min=%dms, max=%dms}",
        operation, count, average, median, min, max);
    }
  }
}
