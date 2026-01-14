package com.kratosgado.blog.backend.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

  private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

  @Pointcut("within(com.kratosgado.blog.backend.controllers..*)")
  public void controllerLayer() {}

  @Pointcut("within(com.kratosgado.blog.backend.services..*)")
  public void serviceLayer() {}

  @Pointcut("within(com.kratosgado.blog.backend.repositories..*)")
  public void repositoryLayer() {}

  @Before("controllerLayer()")
  public void logBeforeController(JoinPoint joinPoint) {
    logger.info(">>> Entering controller method: {} with arguments: {}",
      joinPoint.getSignature().toShortString(),
      Arrays.toString(joinPoint.getArgs())
    );
  }

  @AfterReturning(pointcut = "controllerLayer()", returning = "result")
  public void logAfterController(JoinPoint joinPoint, Object result) {
    logger.info("<<< Completed controller method: {} with result: {}",
      joinPoint.getSignature().toShortString(),
      result != null ? result.getClass().getSimpleName() : "null"
    );
  }

  @AfterThrowing(pointcut = "controllerLayer()", throwing = "exception")
  public void logControllerException(JoinPoint joinPoint, Exception exception) {
    logger.error("!!! Exception in controller method: {} - Message: {}",
      joinPoint.getSignature().toShortString(),
      exception.getMessage(),
      exception
    );
  }

  @Around("serviceLayer()")
  public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
    Instant start = Instant.now();
    String methodName = joinPoint.getSignature().toShortString();
    
    logger.debug("==> Executing service method: {}", methodName);
    
    try {
      Object result = joinPoint.proceed();
      Duration duration = Duration.between(start, Instant.now());
      
      logger.debug("<== Completed service method: {} in {} ms",
        methodName,
        duration.toMillis()
      );
      
      return result;
    } catch (Exception e) {
      Duration duration = Duration.between(start, Instant.now());
      
      logger.error("<== Failed service method: {} after {} ms - Error: {}",
        methodName,
        duration.toMillis(),
        e.getMessage(),
        e
      );
      throw e;
    }
  }

  @Before("repositoryLayer()")
  public void logBeforeRepository(JoinPoint joinPoint) {
    logger.debug(">>> Database operation: {} with parameters: {}",
      joinPoint.getSignature().toShortString(),
      Arrays.toString(joinPoint.getArgs())
    );
  }
}
