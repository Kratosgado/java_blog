package com.kratosgado.blog.backend.aspects;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.kratosgado.blog.backend.services.ViewTrackingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

  private final ViewTrackingService viewTrackingService;

  @Pointcut("within(com.kratosgado.blog.backend.controllers..*)")
  public void controllerLayer() {
  }

  @Pointcut("within(com.kratosgado.blog.backend.graphql..*)")
  public void graphqlControllerLayer() {
  }

  @Pointcut("within(com.kratosgado.blog.backend.services..*)")
  public void serviceLayer() {
  }

  @Pointcut("within(com.kratosgado.blog.backend.repositories..*)")
  public void repositoryLayer() {
  }

  @Before("controllerLayer() || graphqlControllerLayer()")
  public void logBeforeController(JoinPoint joinPoint) {
    log.info(">>> Entering controller method: {} with arguments: {}",
        joinPoint.getSignature().toShortString(),
        Arrays.toString(joinPoint.getArgs()));
  }

  @AfterReturning(pointcut = "controllerLayer() || graphqlControllerLayer()", returning = "result")
  public void logAfterController(JoinPoint joinPoint, Object result) {
    log.info("<<< Completed controller method: {} with result: {}",
        joinPoint.getSignature().toShortString(),
        result != null ? result.getClass().getSimpleName() : "null");
  }

  @AfterThrowing(pointcut = "controllerLayer() || graphqlControllerLayer()", throwing = "exception")
  public void logControllerException(JoinPoint joinPoint, Exception exception) {
    log.error("!!! Exception in controller method: {} - Message: {}",
        joinPoint.getSignature().toShortString(),
        exception.getMessage(),
        exception);
  }

  @Around("serviceLayer()")
  public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
    Instant start = Instant.now();
    String methodName = joinPoint.getSignature().toShortString();

    log.info("==> Executing service method: {}", methodName);

    try {
      Object result = joinPoint.proceed();
      Duration duration = Duration.between(start, Instant.now());

      log.info("<== Completed service method: {} in {} ms",
          methodName,
          duration.toMillis());

      return result;
    } catch (Exception e) {
      Duration duration = Duration.between(start, Instant.now());

      log.error("<== Failed service method: {} after {} ms - Error: {}",
          methodName,
          duration.toMillis(),
          e.getMessage(),
          e);
      throw e;
    }
  }

  @Around("repositoryLayer()")
  public Object logAroundRepository(ProceedingJoinPoint joinPoint) throws Throwable {
    Instant start = Instant.now();
    String methodName = joinPoint.getSignature().toShortString();

    log.debug(">>> Database operation: {} with parameters: {}",
        methodName,
        Arrays.toString(joinPoint.getArgs()));

    try {
      Object result = joinPoint.proceed();
      Duration duration = Duration.between(start, Instant.now());

      log.debug("<<< Completed database operation: {} in {} ms",
          methodName,
          duration.toMillis());

      // Warn about slow queries (> 100ms)
      if (duration.toMillis() > 100) {
        log.warn("!!! Slow database operation detected: {} took {} ms",
            methodName,
            duration.toMillis());
      }

      return result;
    } catch (Exception e) {
      Duration duration = Duration.between(start, Instant.now());

      log.error("<<< Failed database operation: {} after {} ms - Error: {}",
          methodName,
          duration.toMillis(),
          e.getMessage(),
          e);
      throw e;
    }
  }

  /**
   * After viewing a post, asynchronously increment its view count.
   */
  @Async
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.PostService.getPostBySlug(..)) && args(slug)", argNames = "slug")
  public void afterViewingPost(String slug) {
    viewTrackingService.incrementViews(slug);
  }
}
