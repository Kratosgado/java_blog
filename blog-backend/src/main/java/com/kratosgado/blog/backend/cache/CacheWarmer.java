package com.kratosgado.blog.backend.cache;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.kratosgado.blog.backend.cache.CacheConfig.CategoryCache;
import com.kratosgado.blog.backend.cache.CacheConfig.CommentCache;
import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache warmer that pre-loads frequently accessed data on application startup.
 * Listens to ApplicationReadyEvent to ensure all beans are initialized.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmer {
  
  private final PostCache postCache;
  private final CategoryCache categoryCache;
  private final TagCache tagCache;
  private final CommentCache commentCache;
  
  /**
   * Warm up caches when application is ready.
   * This is called after all beans are initialized and the application is ready to serve requests.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void warmUpCaches() {
    log.info("Starting cache warm-up process...");
    long startTime = System.currentTimeMillis();
    
    try {
      // The caches are already loaded during bean initialization
      // This method just logs the stats
      int postCount = postCache.size();
      int categoryCount = categoryCache.size();
      int tagCount = tagCache.size();
      int commentCount = commentCache.size();
      
      long duration = System.currentTimeMillis() - startTime;
      
      log.info("Cache warm-up completed in {}ms", duration);
      log.info("Cache statistics:");
      log.info("  - Posts: {} entries", postCount);
      log.info("  - Categories: {} entries", categoryCount);
      log.info("  - Tags: {} entries", tagCount);
      log.info("  - Comments: {} entries", commentCount);
      log.info("Total cached entries: {}", postCount + categoryCount + tagCount + commentCount);
      
    } catch (Exception e) {
      log.error("Error during cache warm-up", e);
      // Don't fail application startup if cache warm-up fails
    }
  }
}
