package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.dtos.response.MetricsResponse;

public interface MetricsService {
    MetricsResponse getMetrics();
    void recordRequest(long durationMs);
}
