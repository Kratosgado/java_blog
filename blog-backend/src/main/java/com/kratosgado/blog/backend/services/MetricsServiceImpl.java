package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.utils.BlogConstants.Miliseconds;
import com.kratosgado.blog.dtos.response.MetricsResponse;
import com.kratosgado.blog.dtos.response.MetricsResponse.MetricPoint;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsServiceImpl implements MetricsService {

  private final MeterRegistry registry;

  private static final int MAX_HISTORY = 120; // 10 minutes (5s interval)

  // Use thread-safe ConcurrentLinkedQueue for metrics history
  private final Queue<MetricPoint> cpuHistory = new ConcurrentLinkedQueue<>();
  private final Queue<MetricPoint> memoryHistory = new ConcurrentLinkedQueue<>();
  private final Queue<MetricPoint> throughputHistory = new ConcurrentLinkedQueue<>();
  private final Queue<MetricPoint> latencyHistory = new ConcurrentLinkedQueue<>();

  private long lastRequestCount = 0;
  private double lastTotalTime = 0;

  @Override
  public MetricsResponse getMetrics() {
    return new MetricsResponse(
        new ArrayList<>(cpuHistory),
        new ArrayList<>(memoryHistory),
        new ArrayList<>(throughputHistory),
        new ArrayList<>(latencyHistory));
  }

  @Override
  public void recordRequest(long durationMs) {
    // This might be used if we do manual recording,
    // but we rely on Actuator's http.server.requests
  }

  @Scheduled(fixedRate = Miliseconds.FIVE_SECONDS)
  public void collectMetrics() {
    long now = System.currentTimeMillis();

    // 1. CPU Usage
    double cpu = getGaugeValue("system.cpu.usage");
    addPoint(cpuHistory, now, cpu);

    // 2. Memory Usage (Heap used in MB)
    double memory = getGaugeValue("jvm.memory.used", "area", "heap") / (1024 * 1024);
    addPoint(memoryHistory, now, memory);

    // 3. HTTP Traffic (Throughput & Latency)
    // Find the timer for http requests
    Search search = registry.find("http.server.requests");
    Timer timer = search.timer();

    if (timer != null) {
      long count = timer.count();
      double totalTime = timer.totalTime(TimeUnit.MILLISECONDS);

      long deltaCount = count - lastRequestCount;
      double deltaTime = totalTime - lastTotalTime;

      // Throughput (Requests per second)
      // Interval is 5s
      double rps = deltaCount / 5.0;

      // Avg Latency for this interval
      double avgLatency = deltaCount > 0 ? deltaTime / deltaCount : 0;

      addPoint(throughputHistory, now, rps);
      addPoint(latencyHistory, now, avgLatency);

      lastRequestCount = count;
      lastTotalTime = totalTime;

      log.info(
          "Metrics: CPU={}%, Mem={}MB, RPS={}, Latency={}ms",
          String.format("%.2f", cpu * 100),
          String.format("%.2f", memory),
          String.format("%.2f", rps),
          String.format("%.2f", avgLatency));
    } else {
      // Fallback if no traffic yet
      addPoint(throughputHistory, now, 0);
      addPoint(latencyHistory, now, 0);
    }
  }

  private double getGaugeValue(String name, String... tags) {
    try {
      return registry.get(name).tags(tags).gauge().value();
    } catch (Exception e) {
      return 0.0;
    }
  }

  private void addPoint(Queue<MetricPoint> history, long timestamp, double value) {
    history.add(new MetricPoint(timestamp, value));
    while (history.size() > MAX_HISTORY) {
      history.poll();
    }
  }
}
