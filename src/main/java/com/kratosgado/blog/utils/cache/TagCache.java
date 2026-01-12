package com.kratosgado.blog.utils.cache;

import com.kratosgado.blog.models.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory cache for Tag objects with TTL-based expiration.
 * Tags are relatively static, so longer TTL is used.
 */
public class TagCache {
  private static final Logger logger = LoggerFactory.getLogger(TagCache.class);
  private static final TagCache instance = new TagCache();
  
  private final Map<Integer, CachedTag> cache = new ConcurrentHashMap<>();
  private final Map<String, CachedTag> slugCache = new ConcurrentHashMap<>();
  
  // Cache TTL: 30 minutes (tags change infrequently)
  private static final long DEFAULT_TTL_MS = 30 * 60 * 1000;
  
  private TagCache() {
    // Private constructor for singleton
  }
  
  public static TagCache getInstance() {
    return instance;
  }
  
  /**
   * Add tag to cache with default TTL.
   */
  public void put(Tag tag) {
    put(tag, DEFAULT_TTL_MS);
  }
  
  /**
   * Add tag to cache with custom TTL.
   */
  public void put(Tag tag, long ttlMs) {
    if (tag == null || tag.getId() == 0) {
      return;
    }
    
    long expiryTime = System.currentTimeMillis() + ttlMs;
    CachedTag cachedTag = new CachedTag(tag, expiryTime);
    
    cache.put(tag.getId(), cachedTag);
    if (tag.getSlug() != null) {
      slugCache.put(tag.getSlug(), cachedTag);
    }
    
    logger.debug("Cached tag id={}, slug={}", tag.getId(), tag.getSlug());
  }
  
  /**
   * Add multiple tags to cache.
   */
  public void putAll(List<Tag> tags) {
    if (tags == null) {
      return;
    }
    
    for (Tag tag : tags) {
      put(tag);
    }
  }
  
  /**
   * Get tag by ID from cache.
   */
  public Optional<Tag> get(int tagId) {
    CachedTag cached = cache.get(tagId);
    
    if (cached == null) {
      logger.debug("Cache miss for tag id={}", tagId);
      return Optional.empty();
    }
    
    if (cached.isExpired()) {
      logger.debug("Cache expired for tag id={}", tagId);
      invalidate(tagId);
      return Optional.empty();
    }
    
    logger.debug("Cache hit for tag id={}", tagId);
    return Optional.of(cached.tag);
  }
  
  /**
   * Get tag by slug from cache.
   */
  public Optional<Tag> getBySlug(String slug) {
    if (slug == null) {
      return Optional.empty();
    }
    
    CachedTag cached = slugCache.get(slug);
    
    if (cached == null) {
      logger.debug("Cache miss for tag slug={}", slug);
      return Optional.empty();
    }
    
    if (cached.isExpired()) {
      logger.debug("Cache expired for tag slug={}", slug);
      invalidateBySlug(slug);
      return Optional.empty();
    }
    
    logger.debug("Cache hit for tag slug={}", slug);
    return Optional.of(cached.tag);
  }
  
  /**
   * Get all cached tags (non-expired).
   */
  public List<Tag> getAll() {
    List<Tag> tags = new ArrayList<>();
    
    for (CachedTag cached : cache.values()) {
      if (!cached.isExpired()) {
        tags.add(cached.tag);
      }
    }
    
    logger.debug("Retrieved {} tags from cache", tags.size());
    return tags;
  }
  
  /**
   * Remove tag from cache by ID.
   */
  public void invalidate(int tagId) {
    CachedTag removed = cache.remove(tagId);
    if (removed != null && removed.tag.getSlug() != null) {
      slugCache.remove(removed.tag.getSlug());
    }
    logger.debug("Invalidated cache for tag id={}", tagId);
  }
  
  /**
   * Remove tag from cache by slug.
   */
  public void invalidateBySlug(String slug) {
    if (slug == null) {
      return;
    }
    
    CachedTag removed = slugCache.remove(slug);
    if (removed != null) {
      cache.remove(removed.tag.getId());
    }
    logger.debug("Invalidated cache for tag slug={}", slug);
  }
  
  /**
   * Clear all cached tags.
   */
  public void clear() {
    cache.clear();
    slugCache.clear();
    logger.info("Tag cache cleared");
  }
  
  /**
   * Get cache statistics.
   */
  public CacheStats getStats() {
    long expiredCount = cache.values().stream()
      .filter(CachedTag::isExpired)
      .count();
    
    return new CacheStats(cache.size(), expiredCount);
  }
  
  /**
   * Cached tag with expiration time.
   */
  private static class CachedTag {
    final Tag tag;
    final long expiryTime;
    
    CachedTag(Tag tag, long expiryTime) {
      this.tag = tag;
      this.expiryTime = expiryTime;
    }
    
    boolean isExpired() {
      return System.currentTimeMillis() > expiryTime;
    }
  }
  
  /**
   * Cache statistics.
   */
  public static class CacheStats {
    public final int size;
    public final long expiredCount;
    
    public CacheStats(int size, long expiredCount) {
      this.size = size;
      this.expiredCount = expiredCount;
    }
    
    @Override
    public String toString() {
      return String.format("TagCache{size=%d, expired=%d}", size, expiredCount);
    }
  }
}
