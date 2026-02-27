package com.kratosgado.blog.backend.security;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class PerUserRateLimitFilter extends OncePerRequestFilter {

  private final RateLimiterRegistry registry;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    filterChain.doFilter(request, response);
    return;

    // // TODO: uncomment when ready to enable rate limiting
    // if (request.getRequestURI().startsWith("/api/docs")) {
    //   filterChain.doFilter(request, response);
    //   return;
    // }

    // 1. Resolve the Key (User ID or IP)
    // String key = resolveUserKey(request);
    // log.info("Rate limiting request for user: {}", key);
    // RateLimiter rateLimiter = registry.rateLimiter(key);

    // try {
    //   RateLimiter.waitForPermission(rateLimiter);
    //   filterChain.doFilter(request, response);
    // } catch (RequestNotPermitted e) {
    //   response.setStatus(429);
    //   response
    //       .getWriter()
    //       .write("{\"status\": \"429\", \"message\": \"Too many requests for user: " + key +
    // "\"}");
    // }
  }

  private String resolveUserKey(HttpServletRequest request) {
    try {
      return "user:" + SecurityUtils.getCurrentUserId();

    } catch (Exception e) {
      // Fallback to IP if guest
      String ip = request.getHeader("X-Forwarded-For");
      return "ip:" + (ip != null ? ip : request.getRemoteAddr());
    }
  }
}
