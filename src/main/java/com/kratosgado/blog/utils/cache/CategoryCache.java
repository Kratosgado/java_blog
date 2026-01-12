package com.kratosgado.blog.utils.cache;

import com.kratosgado.blog.models.Category;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory cache for Category objects with TTL-based expiration.
 * Categories change infrequently, so we use a longer TTL (15 minutes).
 * Supports lookups by ID and slug.
 */
public class CategoryCache {
  private static final Logger logger = LoggerFactory.getLogger(CategoryCache.class);
  private static final CategoryCache instance = new CategoryCache();
  
  private final Map<Integer, CachedCategory> cache = new ConcurrentHashMap<>();
  private final Map<String, CachedCategory> slugCache = new ConcurrentHashMap<>();
  
  // Cache TTL: 15 minutes (categories change very infrequently)
  private static final long DEFAULT_TTL_MS = 15 * 60 * 1000;
  
  // Special key for "all categories" list cache
  private static final String ALL_CATEGORIES_KEY = "__ALL__";
  private CachedCategoryList allCategoriesCache;
  
  private CategoryCache() {
    // Private constructor for singleton
  }
  
  public static CategoryCache getInstance() {
    return instance;
  }
  
  /**
   * Add category to cache with default TTL.
   */
  public void put(Category category) {
    put(category, DEFAULT_TTL_MS);
  }
  
  /**
   * Add category to cache with custom TTL.
   */
  public void put(Category category, long ttlMs) {
    if (category == null || category.getId() == 0) {
      return;
    }
    
    long expiryTime = System.currentTimeMillis() + ttlMs;
    CachedCategory cachedCategory = new CachedCategory(category, expiryTime);
    
    cache.put(category.getId(), cachedCategory);
    if (category.getSlug() != null) {
      slugCache.put(category.getSlug().toLowerCase(), cachedCategory);
    }
    
    logger.debug("Cached category id={}, slug={}", category.getId(), category.getSlug());
  }
  
  /**
   * Get category by ID from cache.
   */
  public Optional<Category> get(int categoryId) {
    CachedCategory cached = cache.get(categoryId);
    
    if (cached == null) {
      logger.debug("Cache miss for category id={}", categoryId);
      return Optional.empty();
    }
    
    if (cached.isExpired()) {
      logger.debug("Cache expired for category id={}", categoryId);
      invalidate(categoryId);
      return Optional.empty();
    }
    
    logger.debug("Cache hit for category id={}", categoryId);
    return Optional.of(cached.category);
  }
  
  /**
   * Get category by slug from cache.
   */
  public Optional<Category> getBySlug(String slug) {
    if (slug == null) {
      return Optional.empty();
    }
    
    CachedCategory cached = slugCache.get(slug.toLowerCase());
    
    if (cached == null) {
      logger.debug("Cache miss for category slug={}", slug);
      return Optional.empty();
    }
    
    if (cached.isExpired()) {
      logger.debug("Cache expired for category slug={}", slug);
      invalidateBySlug(slug);
      return Optional.empty();
    }
    
    logger.debug("Cache hit for category slug={}", slug);
    return Optional.of(cached.category);
  }
  
  /**
   * Cache all categories list.
   */
  public void putAll(List<Category> categories) {
    putAll(categories, DEFAULT_TTL_MS);
  }
  
  /**
   * Cache all categories list with custom TTL.
   */
  public void putAll(List<Category> categories, long ttlMs) {
    if (categories == null) {
      return;
    }
    
    long expiryTime = System.currentTimeMillis() + ttlMs;
    allCategoriesCache = new CachedCategoryList(categories, expiryTime);
    
    // Also cache individual categories
    for (Category category : categories) {
      put(category, ttlMs);
    }
    
    logger.debug("Cached all categories list, count={}", categories.size());
  }
  
  /**
   * Get all categories from cache.
   */
  public Optional<List<Category>> getAll() {
    if (allCategoriesCache == null) {
      logger.debug("Cache miss for all categories");
      return Optional.empty();
    }
    
    if (allCategoriesCache.isExpired()) {
      logger.debug("Cache expired for all categories");
      allCategoriesCache = null;
      return Optional.empty();
    }
    
    logger.debug("Cache hit for all categories");
    return Optional.of(allCategoriesCache.categories);
  }
  
  /**
   * Remove category from cache by ID.
   */
  public void invalidate(int categoryId) {
    CachedCategory removed = cache.remove(categoryId);
    if (removed != null && removed.category.getSlug() != null) {
      slugCache.remove(removed.category.getSlug().toLowerCase());
    }
    // Invalidate "all categories" list since it changed
    allCategoriesCache = null;
    logger.debug("Invalidated cache for category id={}", categoryId);
  }
  
  /**
   * Remove category from cache by slug.
   */
  public void invalidateBySlug(String slug) {
    if (slug == null) {
      return;
    }
    
    CachedCategory removed = slugCache.remove(slug.toLowerCase());
    if (removed != null) {
      cache.remove(removed.category.getId());
    }
    // Invalidate "all categories" list since it changed
    allCategoriesCache = null;
    logger.debug("Invalidated cache for category slug={}", slug);
  }
  
  /**
   * Clear all cached categories.
   */
  public void clear() {
    cache.clear();
    slugCache.clear();
    allCategoriesCache = null;
    logger.info("Category cache cleared");
  }
  
  /**
   * Get cache statistics.
   */
  public CacheStats getStats() {
    long expiredCount = cache.values().stream()
      .filter(CachedCategory::isExpired)
      .count();
    
    boolean allCachedAndValid = allCategoriesCache != null && !allCategoriesCache.isExpired();
    
    return new CacheStats(cache.size(), expiredCount, allCachedAndValid);
  }
  
  /**
   * Cached category with expiration time.
   */
  private static class CachedCategory {
    final Category category;
    final long expiryTime;
    
    CachedCategory(Category category, long expiryTime) {
      this.category = category;
      this.expiryTime = expiryTime;
    }
    
    boolean isExpired() {
      return System.currentTimeMillis() > expiryTime;
    }
  }
  
  /**
   * Cached category list with expiration time.
   */
  private static class CachedCategoryList {
    final List<Category> categories;
    final long expiryTime;
    
    CachedCategoryList(List<Category> categories, long expiryTime) {
      this.categories = categories;
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
    public final boolean allCategoriesCached;
    
    public CacheStats(int size, long expiredCount, boolean allCategoriesCached) {
      this.size = size;
      this.expiredCount = expiredCount;
      this.allCategoriesCached = allCategoriesCached;
    }
    
    @Override
    public String toString() {
      return String.format("CategoryCache{size=%d, expired=%d, allCached=%b}", 
        size, expiredCount, allCategoriesCached);
    }
  }
}
