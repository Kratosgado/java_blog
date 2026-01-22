package com.kratosgado.blog.backend.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.cache.CacheConfig.CategoryCache;
import com.kratosgado.blog.backend.cache.CacheConfig.CommentCache;
import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.backend.cache.ConcurrentMapCache.CacheStats;
import com.kratosgado.blog.dtos.response.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing and monitoring application caches.
 * Provides endpoints to view cache statistics and manually trigger cache
 * operations.
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "Cache monitoring and management APIs")
public class CacheController {

  private final PostCache postCache;
  private final CategoryCache categoryCache;
  private final TagCache tagCache;
  private final CommentCache commentCache;

  /**
   * Get statistics for all caches.
   */
  @GetMapping("/stats")
  @Operation(summary = "Get cache statistics", description = "Retrieves statistics for all application caches. Requires admin role.", security = @SecurityRequirement(name = "bearer-jwt"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseDto<Map<String, CacheStats>>> getCacheStats() {
    log.info("Fetching cache statistics");

    Map<String, CacheStats> stats = new HashMap<>();
    stats.put("posts", postCache.getStats());
    stats.put("categories", categoryCache.getStats());
    stats.put("tags", tagCache.getStats());
    stats.put("comments", commentCache.getStats());

    return ResponseEntity.ok(ResponseDto.success("Cache statistics retrieved successfully", stats));
  }

  /**
   * Clear a specific cache.
   */
  @DeleteMapping("/{cacheName}")
  @Operation(summary = "Clear a cache", description = "Clears all entries from the specified cache. Requires admin role.", security = @SecurityRequirement(name = "bearer-jwt"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseDto<String>> clearCache(@PathVariable String cacheName) {
    log.info("Clearing cache: {}", cacheName);

    switch (cacheName.toLowerCase()) {
      case "posts":
        postCache.clear();
        break;
      case "categories":
        categoryCache.clear();
        break;
      case "tags":
        tagCache.clear();
        break;
      case "comments":
        commentCache.clear();
        break;
      default:
        return ResponseEntity.badRequest()
            .body(ResponseDto.error("Invalid cache name. Valid options: posts, categories, tags, comments"));
    }

    return ResponseEntity.ok(ResponseDto.success("Cache cleared successfully", cacheName));
  }

  /**
   * Clear all caches.
   */
  @DeleteMapping
  @Operation(summary = "Clear all caches", description = "Clears all application caches. Requires admin role.", security = @SecurityRequirement(name = "bearer-jwt"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseDto<String>> clearAllCaches() {
    log.info("Clearing all caches");

    postCache.clear();
    categoryCache.clear();
    tagCache.clear();
    commentCache.clear();

    return ResponseEntity.ok(ResponseDto.success("All caches cleared successfully", "all"));
  }

  /**
   * Refresh a specific cache.
   */
  @PostMapping("/{cacheName}/refresh")
  @Operation(summary = "Refresh a cache", description = "Refreshes the specified cache by reloading data. Requires admin role.", security = @SecurityRequirement(name = "bearer-jwt"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseDto<String>> refreshCache(@PathVariable String cacheName) {
    log.info("Refreshing cache: {}", cacheName);

    switch (cacheName.toLowerCase()) {
      case "posts":
        postCache.refresh();
        break;
      case "categories":
        categoryCache.refresh();
        break;
      case "tags":
        tagCache.refresh();
        break;
      case "comments":
        commentCache.refresh();
        break;
      default:
        return ResponseEntity.badRequest()
            .body(ResponseDto.error("Invalid cache name. Valid options: posts, categories, tags, comments"));
    }

    return ResponseEntity.ok(ResponseDto.success("Cache refreshed successfully", cacheName));
  }

  /**
   * Evict a specific entry from cache.
   */
  @DeleteMapping("/{cacheName}/{key}")
  @Operation(summary = "Evict cache entry", description = "Evicts a specific entry from the cache. Requires admin role.", security = @SecurityRequirement(name = "bearer-jwt"))
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseDto<String>> evictCacheEntry(
      @PathVariable String cacheName,
      @PathVariable String key) {

    log.info("Evicting entry from cache: {} with key: {}", cacheName, key);

    try {
      switch (cacheName.toLowerCase()) {
        case "posts":
        case "categories":
        case "tags":
          Long longKey = Long.parseLong(key);
          if (cacheName.equalsIgnoreCase("posts")) {
            postCache.evict(key);
          } else if (cacheName.equalsIgnoreCase("categories")) {
            categoryCache.evict(longKey);
          } else {
            tagCache.evict(longKey);
          }
          break;
        case "comments":
          commentCache.evict(key);
          break;
        default:
          return ResponseEntity.badRequest()
              .body(ResponseDto.error("Invalid cache name"));
      }

      return ResponseEntity.ok(ResponseDto.success("Cache entry evicted successfully", key));

    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest()
          .body(ResponseDto.error("Invalid key format for cache: " + cacheName));
    }
  }
}
