package com.kratosgado.blog.backend.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // Core pool size: Number of threads to keep in the pool, even if they are idle.
    // Set to 20 to handle a moderate number of concurrent requests without delay.
    executor.setCorePoolSize(20);
    // Max pool size: Maximum number of threads to allow in the pool.
    // Set to 100 to handle bursts of traffic.
    executor.setMaxPoolSize(100);
    // Queue capacity: The queue to use for holding tasks before they are executed.
    // Set to 500 to buffer tasks when all core threads are busy.
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("BlogBackend-");
    executor.initialize();
    return executor;
  }
}
