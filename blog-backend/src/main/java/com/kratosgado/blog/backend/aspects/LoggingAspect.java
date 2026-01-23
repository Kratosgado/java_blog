package com.kratosgado.blog.backend.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
  @Pointcut("within(com.kratosgado.blog.backend.controllers..*)")
  public void controllerLayer() {
  }

  @Pointcut("within(com.kratosgado.blog.backend.services..*)")
  public void serviceLayer() {
  }

  @Pointcut("within(com.kratosgado.blog.backend.dao..*)")
  public void daoLayer() {
  }

  @Before("controllerLayer()")
  public void logBeforeController(JoinPoint joinPoint) {
    log.info(">>> Entering controller method: {} with arguments: {}",
        joinPoint.getSignature().toShortString(),
        Arrays.toString(joinPoint.getArgs()));
  }

  @AfterReturning(pointcut = "controllerLayer()", returning = "result")
  public void logAfterController(JoinPoint joinPoint, Object result) {
    log.info("<<< Completed controller method: {} with result: {}",
        joinPoint.getSignature().toShortString(),
        result != null ? result.getClass().getSimpleName() : "null");
  }

  @AfterThrowing(pointcut = "controllerLayer()", throwing = "exception")
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

  @Around("daoLayer()")
  public Object logAroundDao(ProceedingJoinPoint joinPoint) throws Throwable {
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
}
