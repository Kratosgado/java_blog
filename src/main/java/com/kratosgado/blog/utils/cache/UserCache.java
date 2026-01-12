package com.kratosgado.blog.utils.cache;

import com.kratosgado.blog.models.User;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory cache for User objects with TTL-based expiration.
 * Uses singleton pattern for global access.
 */
public class UserCache {
  private static final Logger logger = LoggerFactory.getLogger(UserCache.class);
  private static final UserCache instance = new UserCache();
  
  private final Map<Integer, CachedUser> cache = new ConcurrentHashMap<>();
  private final Map<String, CachedUser> emailCache = new ConcurrentHashMap<>();
  
  // Cache TTL: 10 minutes (users change less frequently)
  private static final long DEFAULT_TTL_MS = 10 * 60 * 1000;
  
  private UserCache() {
    // Private constructor for singleton
  }
  
  public static UserCache getInstance() {
    return instance;
  }
  
  /**
   * Add user to cache with default TTL.
   */
  public void put(User user) {
    put(user, DEFAULT_TTL_MS);
  }
  
  /**
   * Add user to cache with custom TTL.
   */
  public void put(User user, long ttlMs) {
    if (user == null || user.getId() == 0) {
      return;
    }
    
    long expiryTime = System.currentTimeMillis() + ttlMs;
    CachedUser cachedUser = new CachedUser(user, expiryTime);
    
    cache.put(user.getId(), cachedUser);
    if (user.getEmail() != null) {
      emailCache.put(user.getEmail().toLowerCase(), cachedUser);
    }
    
    logger.debug("Cached user id={}, email={}", user.getId(), user.getEmail());
  }
  
  /**
   * Get user by ID from cache.
   */
  public Optional<User> get(int userId) {
    CachedUser cached = cache.get(userId);
    
    if (cached == null) {
      logger.debug("Cache miss for user id={}", userId);
      return Optional.empty();
    }
    
    if (cached.isExpired()) {
      logger.debug("Cache expired for user id={}", userId);
      invalidate(userId);
      return Optional.empty();
    }
    
    logger.debug("Cache hit for user id={}", userId);
    return Optional.of(cached.user);
  }
  
  /**
   * Get user by email from cache.
   */
  public Optional<User> getByEmail(String email) {
    if (email == null) {
      return Optional.empty();
    }
    
    CachedUser cached = emailCache.get(email.toLowerCase());
    
    if (cached == null) {
      logger.debug("Cache miss for user email={}", email);
      return Optional.empty();
    }
    
    if (cached.isExpired()) {
      logger.debug("Cache expired for user email={}", email);
      invalidateByEmail(email);
      return Optional.empty();
    }
    
    logger.debug("Cache hit for user email={}", email);
    return Optional.of(cached.user);
  }
  
  /**
   * Remove user from cache by ID.
   */
  public void invalidate(int userId) {
    CachedUser removed = cache.remove(userId);
    if (removed != null && removed.user.getEmail() != null) {
      emailCache.remove(removed.user.getEmail().toLowerCase());
    }
    logger.debug("Invalidated cache for user id={}", userId);
  }
  
  /**
   * Remove user from cache by email.
   */
  public void invalidateByEmail(String email) {
    if (email == null) {
      return;
    }
    
    CachedUser removed = emailCache.remove(email.toLowerCase());
    if (removed != null) {
      cache.remove(removed.user.getId());
    }
    logger.debug("Invalidated cache for user email={}", email);
  }
  
  /**
   * Clear all cached users.
   */
  public void clear() {
    cache.clear();
    emailCache.clear();
    logger.info("User cache cleared");
  }
  
  /**
   * Get cache statistics.
   */
  public CacheStats getStats() {
    long expiredCount = cache.values().stream()
      .filter(CachedUser::isExpired)
      .count();
    
    return new CacheStats(cache.size(), expiredCount);
  }
  
  /**
   * Cached user with expiration time.
   */
  private static class CachedUser {
    final User user;
    final long expiryTime;
    
    CachedUser(User user, long expiryTime) {
      this.user = user;
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
      return String.format("UserCache{size=%d, expired=%d}", size, expiredCount);
    }
  }
}
