package com.kratosgado.blog.backend.aspects;

import com.kratosgado.blog.backend.performance.QueryPerformanceMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for monitoring repository method performance.
 * Automatically tracks execution time of all repository method calls.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RepositoryPerformanceAspect {
  private final QueryPerformanceMonitor performanceMonitor;

  /**
   * Monitor all repository method executions.
   * Captures execution time and records metrics for analysis.
   */
  @Around("execution(* com.kratosgado.blog.backend.repositories..*(..))")
  public Object monitorRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String queryName = className + "." + methodName;

    long startTime = System.nanoTime();
    try {
      Object result = joinPoint.proceed();
      long duration = System.nanoTime() - startTime;
      performanceMonitor.recordQuery(queryName, duration);
      return result;
    } catch (Throwable t) {
      long duration = System.nanoTime() - startTime;
      performanceMonitor.recordQuery(queryName + ".ERROR", duration);
      throw t;
    }
  }
}
