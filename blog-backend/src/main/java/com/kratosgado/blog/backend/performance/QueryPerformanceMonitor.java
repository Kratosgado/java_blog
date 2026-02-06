package com.kratosgado.blog.backend.performance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Monitors and records query performance metrics for analysis and optimization.
 * Tracks execution times, call counts, and calculates statistics for repository queries.
 */
@Slf4j
@Component
public class QueryPerformanceMonitor {
  private final ConcurrentHashMap<String, QueryMetrics> metricsMap = new ConcurrentHashMap<>();
  private final ThreadLocal<Long> startTime = new ThreadLocal<>();

  /**
   * Start timing a query execution.
   *
   * @param queryName Unique identifier for the query
   */
  public void startQuery(String queryName) {
    startTime.set(System.nanoTime());
  }

  /**
   * End timing and record the query execution.
   *
   * @param queryName Unique identifier for the query
   */
  public void endQuery(String queryName) {
    Long start = startTime.get();
    if (start != null) {
      long duration = System.nanoTime() - start;
      recordQuery(queryName, duration);
      startTime.remove();
    }
  }

  /**
   * Record a query execution with its duration.
   *
   * @param queryName Unique identifier for the query
   * @param durationNanos Duration in nanoseconds
   */
  public void recordQuery(String queryName, long durationNanos) {
    metricsMap.computeIfAbsent(queryName, k -> new QueryMetrics()).addExecution(durationNanos);

    long durationMs = durationNanos / 1_000_000;
    if (durationMs > 100) { // Log slow queries
      log.warn("Slow query detected: {} took {}ms", queryName, durationMs);
    }
  }

  /**
   * Get metrics for a specific query.
   *
   * @param queryName Unique identifier for the query
   * @return QueryMetrics or null if not found
   */
  public QueryMetrics getMetrics(String queryName) {
    return metricsMap.get(queryName);
  }

  /**
   * Get all recorded metrics.
   *
   * @return Map of query names to their metrics
   */
  public ConcurrentHashMap<String, QueryMetrics> getAllMetrics() {
    return new ConcurrentHashMap<>(metricsMap);
  }

  /**
   * Reset all metrics.
   */
  public void resetMetrics() {
    metricsMap.clear();
    log.info("Query performance metrics reset");
  }

  /**
   * Print a performance report to logs.
   */
  public void printReport() {
    log.info("=== Query Performance Report ===");
    metricsMap.forEach(
        (queryName, metrics) -> {
          log.info(
              "Query: {} | Calls: {} | Avg: {}ms | Min: {}ms | Max: {}ms | Total: {}ms",
              queryName,
              metrics.getCallCount(),
              metrics.getAverageDuration() / 1_000_000,
              metrics.getMinDuration() / 1_000_000,
              metrics.getMaxDuration() / 1_000_000,
              metrics.getTotalDuration() / 1_000_000);
        });
    log.info("================================");
  }

  /** Metrics container for a single query. */
  @Data
  public static class QueryMetrics {
    private final LongAdder callCount = new LongAdder();
    private final LongAdder totalDuration = new LongAdder(); // in nanoseconds
    private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxDuration = new AtomicLong(0);

    public void addExecution(long durationNanos) {
      callCount.increment();
      totalDuration.add(durationNanos);
      updateMin(durationNanos);
      updateMax(durationNanos);
    }

    private void updateMin(long duration) {
      minDuration.updateAndGet(current -> Math.min(current, duration));
    }

    private void updateMax(long duration) {
      maxDuration.updateAndGet(current -> Math.max(current, duration));
    }

    public long getCallCount() {
      return callCount.sum();
    }

    public long getTotalDuration() {
      return totalDuration.sum();
    }

    public long getMinDuration() {
      long min = minDuration.get();
      return min == Long.MAX_VALUE ? 0 : min;
    }

    public long getMaxDuration() {
      return maxDuration.get();
    }

    public double getAverageDuration() {
      long count = getCallCount();
      return count > 0 ? (double) getTotalDuration() / count : 0;
    }
  }
}
