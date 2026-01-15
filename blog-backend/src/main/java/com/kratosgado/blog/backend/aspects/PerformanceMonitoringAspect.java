package com.kratosgado.blog.backend.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
public class PerformanceMonitoringAspect {

  private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
  private static final long SLOW_THRESHOLD_MS = 1000;

  @Pointcut("execution(* com.kratosgado.blog.backend.services..*(..))")
  public void serviceMethods() {
  }

  @Pointcut("execution(* com.kratosgado.blog.backend.repositories..*(..))")
  public void repositoryMethods() {
  }

  @Around("serviceMethods() || repositoryMethods()")
  public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
    Instant start = Instant.now();
    String methodName = joinPoint.getSignature().toShortString();

    try {
      Object result = joinPoint.proceed();
      Duration duration = Duration.between(start, Instant.now());
      long executionTime = duration.toMillis();

      if (executionTime > SLOW_THRESHOLD_MS) {
        logger.warn("SLOW OPERATION: {} took {} ms (threshold: {} ms)",
            methodName,
            executionTime,
            SLOW_THRESHOLD_MS);
      } else {
        logger.debug("Performance: {} executed in {} ms",
            methodName,
            executionTime);
      }

      return result;
    } catch (Exception e) {
      Duration duration = Duration.between(start, Instant.now());
      logger.error("Failed operation: {} after {} ms",
          methodName,
          duration.toMillis());
      throw e;
    }
  }
}
