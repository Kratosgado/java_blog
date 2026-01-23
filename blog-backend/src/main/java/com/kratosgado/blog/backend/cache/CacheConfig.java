package com.kratosgado.blog.backend.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kratosgado.blog.backend.utils.BlogConstants.Miliseconds;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for initializing and managing application caches.
 * Provides beans for different entity caches with automatic refresh
 * capabilities.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CacheConfig {
  /**
   * Post cache bean with key as Post ID.
   * Caches PostResponse objects for quick retrieval.
   */
  @Bean
  public PostCache postCache() {
    log.info("Initializing Post cache");
    PostCache cache = new PostCache("PostCache", Miliseconds.FIVE_MINUTES);
    return cache;
  }

  /**
   * Category cache bean with key as Category ID.
   */
  @Bean
  public CategoryCache categoryCache() {
    log.info("Initializing Category cache");
    CategoryCache cache = new CategoryCache("CategoryCache", Miliseconds.THIRTY_MINUTES);
    return cache;
  }

  /**
   * Tag cache bean with key as Tag ID.
   */
  @Bean
  public TagCache tagCache() {
    log.info("Initializing Tag cache");
    TagCache cache = new TagCache("TagCache", Miliseconds.THIRTY_MINUTES);
    return cache;
  }

  /**
   * Specific cache implementations
   */
  public static class PostCache extends ConcurrentMapCache<String, PostResponse> {
    public PostCache(String cacheName, long ttlMillis) {
      super(cacheName, ttlMillis);
    }
  }

  public static class CategoryCache extends ConcurrentMapCache<Long, Category> {
    public CategoryCache(String cacheName, long ttlMillis) {
      super(cacheName, ttlMillis);
    }
  }

  public static class TagCache extends ConcurrentMapCache<Long, Tag> {
    public TagCache(String cacheName, long ttlMillis) {
      super(cacheName, ttlMillis);
    }
  }

}
