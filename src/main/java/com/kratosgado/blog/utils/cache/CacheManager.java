package com.kratosgado.blog.utils.cache;

import com.kratosgado.blog.utils.cache.CategoryCache;
import com.kratosgado.blog.utils.cache.PostCache;
import com.kratosgado.blog.utils.cache.TagCache;
import com.kratosgado.blog.utils.cache.UserCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized cache monitoring and management utility.
 * Provides statistics and control for all application caches.
 */
public class CacheManager {
  private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
  private static final CacheManager instance = new CacheManager();
  
  private CacheManager() {
    // Private constructor for singleton
  }
  
  public static CacheManager getInstance() {
    return instance;
  }
  
  /**
   * Get comprehensive statistics for all caches.
   */
  public AllCacheStats getAllStats() {
    return new AllCacheStats(
      UserCache.getInstance().getStats(),
      PostCache.getInstance().getStats(),
      CategoryCache.getInstance().getStats(),
      TagCache.getInstance().getStats()
    );
  }
  
  /**
   * Clear all application caches.
   */
  public void clearAll() {
    UserCache.getInstance().clear();
    PostCache.getInstance().clear();
    CategoryCache.getInstance().clear();
    TagCache.getInstance().clear();
    logger.info("All caches cleared");
  }
  
  /**
   * Log current cache statistics at INFO level.
   */
  public void logStats() {
    AllCacheStats stats = getAllStats();
    logger.info("=== Cache Statistics ===");
    logger.info("Users:      {}", stats.userStats);
    logger.info("Posts:      {}", stats.postStats);
    logger.info("Categories: {}", stats.categoryStats);
    logger.info("Tags:       {}", stats.tagStats);
    logger.info("Total Size: {}", stats.getTotalSize());
    logger.info("Total Expired: {}", stats.getTotalExpired());
    logger.info("========================");
  }
  
  /**
   * Get cache efficiency metrics (hit rate would require tracking hits/misses).
   * Returns total active entries across all caches.
   */
  public long getTotalActiveEntries() {
    AllCacheStats stats = getAllStats();
    return stats.getTotalSize() - stats.getTotalExpired();
  }
  
  /**
   * Comprehensive statistics for all caches.
   */
  public static class AllCacheStats {
    public final UserCache.CacheStats userStats;
    public final PostCache.CacheStats postStats;
    public final CategoryCache.CacheStats categoryStats;
    public final TagCache.CacheStats tagStats;
    
    public AllCacheStats(
        UserCache.CacheStats userStats,
        PostCache.CacheStats postStats,
        CategoryCache.CacheStats categoryStats,
        TagCache.CacheStats tagStats) {
      this.userStats = userStats;
      this.postStats = postStats;
      this.categoryStats = categoryStats;
      this.tagStats = tagStats;
    }
    
    public int getTotalSize() {
      return userStats.size + postStats.totalSize + categoryStats.size + tagStats.size;
    }
    
    public long getTotalExpired() {
      return userStats.expiredCount + postStats.expiredCount + 
             categoryStats.expiredCount + tagStats.expiredCount;
    }
    
    @Override
    public String toString() {
      return String.format(
        "AllCacheStats{users=%s, posts=%s, categories=%s, tags=%s, total=%d, expired=%d}",
        userStats, postStats, categoryStats, tagStats, getTotalSize(), getTotalExpired()
      );
    }
  }
}
