package com.kratosgado.blog.backend.config;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

  @PostConstruct
  public void init() {
    // Enable SecurityContext propagation to child threads
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
  }

  @Bean(name = {"taskExecutor", "asyncExecutor"})
  @Primary
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(30);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("BlogAsync-");
    executor.initialize();
    
    // Wrap executor to propagate SecurityContext
    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
  }

  @Override
  public Executor getAsyncExecutor() {
    // Return the same bean for @Async methods
    return taskExecutor();
  }
}
