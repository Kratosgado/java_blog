package com.kratosgado.blog.dtos.response;

import java.util.List;

public record MetricsResponse(
    List<MetricPoint> cpuUsage,
    List<MetricPoint> memoryUsage,
    List<MetricPoint> activeRequests,
    List<MetricPoint> responseTime) {
  public record MetricPoint(long timestamp, double value) {}
}
