package com.kratosgado.blog.backend.utils.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PerformanceMonitorTest {

  private PerformanceMonitor monitor;

  @BeforeEach
  void setUp() {
    monitor = PerformanceMonitor.getInstance();
    monitor.reset();
    monitor.setEnabled(true);
  }

  @Test
  void testConcurrentRecording() throws InterruptedException {
    int numThreads = 10;
    int operationsPerThread = 1000;
    String opName = "test-concurrent-op";

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    CountDownLatch latch = new CountDownLatch(numThreads);
    AtomicInteger exceptions = new AtomicInteger(0);

    for (int i = 0; i < numThreads; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < operationsPerThread; j++) {
            monitor.measure(opName, () -> {
              try {
                Thread.sleep(1);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              return "result";
            });
          }
        } catch (Exception e) {
          exceptions.incrementAndGet();
          e.printStackTrace();
        } finally {
          latch.countDown();
        }
      });
    }

    boolean completed = latch.await(10, TimeUnit.SECONDS);
    assertTrue(completed, "Test timed out");
    assertEquals(0, exceptions.get(), "Exceptions occurred during concurrent execution");

    PerformanceMonitor.OperationStats stats = monitor.getStats(opName);
    assertEquals(numThreads * operationsPerThread, stats.count, "Count mismatch");
    assertTrue(stats.min >= 1, "Min duration should be at least sleep time");
    
    executor.shutdown();
  }

  @Test
  void testMeasureVoid() {
    String opName = "test-void-op";
    monitor.measureVoid(opName, () -> {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    PerformanceMonitor.OperationStats stats = monitor.getStats(opName);
    assertEquals(1, stats.count);
    assertTrue(stats.average >= 10);
  }

  @Test
  void testReset() {
    String opName = "test-reset";
    monitor.measureVoid(opName, () -> {});
    assertEquals(1, monitor.getStats(opName).count);

    monitor.reset();
    assertEquals(0, monitor.getStats(opName).count);
  }
}
