package com.kratosgado.blog.utils.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Post;

/**
 * In-memory caching layer for posts with TTL support.
 * Implements a simple cache with automatic invalidation on updates.
 */
public class PostCache {
  private static final Logger logger = LoggerFactory.getLogger(PostCache.class);
  private static final long DEFAULT_TTL_MILLIS = 5 * 60 * 1000; // 5 minutes
  private static final PostCache INSTANCE = new PostCache();

  private final Map<Integer, CacheEntry> cache = new HashMap<>();
  private final long ttl;

  private PostCache() {
    this.ttl = DEFAULT_TTL_MILLIS;
  }

  public static PostCache getInstance() {
    return INSTANCE;
  }

  /**
   * Put a post in cache with automatic TTL expiration
   */
  public void put(int postId, Post post) {
    cache.put(postId, new CacheEntry(post, System.currentTimeMillis()));
    logger.debug("Post {} cached", postId);
  }

  /**
   * Get a post from cache if it exists and hasn't expired
   */
  public Optional<Post> get(int postId) {
    CacheEntry entry = cache.get(postId);
    if (entry == null) {
      logger.debug("Cache miss for post {}", postId);
      return Optional.empty();
    }

    if (isExpired(entry)) {
      cache.remove(postId);
      logger.debug("Cache expired for post {}", postId);
      return Optional.empty();
    }

    logger.debug("Cache hit for post {}", postId);
    return Optional.of(entry.post);
  }

  /**
   * Invalidate cache entry (typically called on update)
   */
  public void invalidate(int postId) {
    cache.remove(postId);
    logger.debug("Cache invalidated for post {}", postId);
  }

  /**
   * Clear entire cache
   */
  public void clear() {
    cache.clear();
    logger.info("Post cache cleared");
  }

  /**
   * Get cache statistics
   */
  public CacheStats getStats() {
    int size = cache.size();
    int expired = (int) cache.values().stream()
        .filter(this::isExpired)
        .count();
    return new CacheStats(size, expired);
  }

  private boolean isExpired(CacheEntry entry) {
    return (System.currentTimeMillis() - entry.timestamp) > ttl;
  }

  /**
   * Inner class to store cache entry with timestamp
   */
  private static class CacheEntry {
    Post post;
    long timestamp;

    CacheEntry(Post post, long timestamp) {
      this.post = post;
      this.timestamp = timestamp;
    }
  }

  /**
   * Cache statistics holder
   */
  public static class CacheStats {
    public final int totalSize;
    public final int expiredCount;

    CacheStats(int totalSize, int expiredCount) {
      this.totalSize = totalSize;
      this.expiredCount = expiredCount;
    }

    @Override
    public String toString() {
      return String.format("CacheStats{size=%d, expired=%d}", totalSize, expiredCount);
    }
  }
}
