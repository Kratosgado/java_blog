package com.kratosgado.blog.backend.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.kratosgado.blog.backend.utils.performance.PerformanceMonitor;

/**
 * Aspect for tracking performance metrics of Repository and Service operations.
 * Integrates with PerformanceMonitor to collect and report timing statistics.
 */
@Aspect
@Component
public class PerformanceAspect {
  
  private final PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
  
  @Pointcut("within(com.kratosgado.blog.backend.repositories..*)")
  public void repositoryLayer() {
  }
  
  @Pointcut("within(com.kratosgado.blog.backend.services..*)")
  public void serviceLayer() {
  }
  
  /**
   * Track performance of all Repository operations (JPA and MongoDB).
   */
  @Around("repositoryLayer()")
  public Object trackRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String operation = String.format("Repository.%s.%s", className, methodName);
    
    return performanceMonitor.measure(operation, () -> {
      try {
        return joinPoint.proceed();
      } catch (Throwable e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
      }
    });
  }
  
  /**
   * Track performance of all service operations.
   */
  @Around("serviceLayer()")
  public Object trackServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String operation = String.format("Service.%s.%s", className, methodName);
    
    return performanceMonitor.measure(operation, () -> {
      try {
        return joinPoint.proceed();
      } catch (Throwable e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
      }
    });
  }
}
