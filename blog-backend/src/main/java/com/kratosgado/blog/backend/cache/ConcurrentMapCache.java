package com.kratosgado.blog.backend.cache;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.response.PageResponse;

/**
 * Generic cache implementation using ConcurrentHashMap for thread-safe caching
 * with support for efficient searching, sorting, and pagination.
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
public class ConcurrentMapCache<K, V> implements CacheService<K, V> {

  private static final Logger log = LoggerFactory.getLogger(ConcurrentMapCache.class);

  private final ConcurrentHashMap<K, V> cache;
  private final ConcurrentHashMap<K, LocalDateTime> cacheTimestamps;
  private final Supplier<List<V>> dataLoader;
  private final long ttlMillis;
  private final String cacheName;

  // Metrics tracking
  private long hits = 0;
  private long misses = 0;
  private long evictions = 0;

  /**
   * Constructor with TTL support.
   * 
   * @param cacheName  name of the cache for logging
   * @param dataLoader supplier to reload cache data
   * @param ttlMillis  time-to-live in milliseconds (0 for no expiration)
   */
  public ConcurrentMapCache(String cacheName, Supplier<List<V>> dataLoader, long ttlMillis) {
    this.cache = new ConcurrentHashMap<>();
    this.cacheTimestamps = new ConcurrentHashMap<>();
    this.dataLoader = dataLoader;
    this.ttlMillis = ttlMillis;
    this.cacheName = cacheName;
    log.info("Initialized cache: {} with TTL: {}ms", cacheName, ttlMillis);
  }

  /**
   * Constructor without TTL.
   * 
   * @param cacheName  name of the cache for logging
   * @param dataLoader supplier to reload cache data
   */
  public ConcurrentMapCache(String cacheName, Supplier<List<V>> dataLoader) {
    this(cacheName, dataLoader, 0);
  }

  @Override
  public void put(K key, V value) {
    cache.put(key, value);
    cacheTimestamps.put(key, LocalDateTime.now());
    log.debug("Cache [{}]: Put key {}", cacheName, key);
  }

  @Override
  public void updateIfPresent(K key, V value) {
    if (cache.containsKey(key))
      put(key, value);
  }

  @Override
  public Optional<V> get(K key) {
    // Check if entry has expired
    if (ttlMillis > 0 && cacheTimestamps.containsKey(key)) {
      LocalDateTime timestamp = cacheTimestamps.get(key);
      long ageMillis = java.time.Duration.between(timestamp, LocalDateTime.now()).toMillis();
      if (ageMillis > ttlMillis) {
        evict(key);
        log.debug("Cache [{}]: Entry expired for key {}", cacheName, key);
        misses++;
        return Optional.empty();
      }
    }

    V value = cache.get(key);
    if (value != null) {
      hits++;
      log.debug("Cache [{}]: Get key {} - HIT", cacheName, key);
    } else {
      misses++;
      log.debug("Cache [{}]: Get key {} - MISS", cacheName, key);
    }
    return Optional.ofNullable(value);
  }

  @Override
  public void evict(K key) {
    cache.remove(key);
    cacheTimestamps.remove(key);
    evictions++;
    log.debug("Cache [{}]: Evicted key {}", cacheName, key);
  }

  @Override
  public void clear() {
    int size = cache.size();
    cache.clear();
    cacheTimestamps.clear();
    evictions += size;
    log.info("Cache [{}]: Cleared {} entries", cacheName, size);
  }

  @Override
  public List<V> getAll() {
    // Remove expired entries first
    if (ttlMillis > 0) {
      removeExpiredEntries();
    }
    return new ArrayList<>(cache.values());
  }

  @Override
  public PageResponse<V> search(Predicate<V> searchPredicate, int page, int size,
      String sortField, boolean ascending) {

    log.debug("Cache [{}]: Searching with page={}, size={}, sortField={}, ascending={}",
        cacheName, page, size, sortField, ascending);

    // Remove expired entries first
    if (ttlMillis > 0) {
      removeExpiredEntries();
    }

    // Apply search predicate
    List<V> filtered = cache.values().stream()
        .filter(searchPredicate)
        .collect(Collectors.toList());

    // Apply sorting if specified
    if (sortField != null && !sortField.isEmpty()) {
      filtered = applySorting(filtered, sortField, ascending);
    }

    // Apply pagination
    return paginateList(filtered, page, size);
  }

  @Override
  public PageResponse<V> paginate(int page, int size, String sortField, boolean ascending) {
    log.debug("Cache [{}]: Paginating with page={}, size={}, sortField={}, ascending={}",
        cacheName, page, size, sortField, ascending);

    // Remove expired entries first
    if (ttlMillis > 0) {
      removeExpiredEntries();
    }

    List<V> values = new ArrayList<>(cache.values());

    // Apply sorting if specified
    if (sortField != null && !sortField.isEmpty()) {
      values = applySorting(values, sortField, ascending);
    }

    // Apply pagination
    return paginateList(values, page, size);
  }

  @Override
  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  @Override
  public int size() {
    // Remove expired entries first
    if (ttlMillis > 0) {
      removeExpiredEntries();
    }
    return cache.size();
  }

  @Override
  public void refresh() {
    log.info("Cache [{}]: Refreshing cache", cacheName);
    clear();

    if (dataLoader != null) {
      try {
        List<V> data = dataLoader.get();
        log.info("Cache [{}]: Loaded {} entries", cacheName, data.size());
        // Note: This requires a way to extract keys from values
        // Subclasses should override this method to properly map keys
      } catch (Exception e) {
        log.error("Cache [{}]: Error refreshing cache", cacheName, e);
      }
    }
  }

  /**
   * Remove expired entries from the cache.
   */
  private void removeExpiredEntries() {
    LocalDateTime now = LocalDateTime.now();
    List<K> expiredKeys = new ArrayList<>();

    cacheTimestamps.forEach((key, timestamp) -> {
      long ageMillis = java.time.Duration.between(timestamp, now).toMillis();
      if (ageMillis > ttlMillis) {
        expiredKeys.add(key);
      }
    });

    expiredKeys.forEach(this::evict);

    if (!expiredKeys.isEmpty()) {
      log.debug("Cache [{}]: Removed {} expired entries", cacheName, expiredKeys.size());
    }
  }

  /**
   * Apply sorting to a list using reflection.
   * Uses binary search tree approach for efficient sorting.
   */
  private List<V> applySorting(List<V> list, String sortField, boolean ascending) {
    try {
      Comparator<V> comparator = (v1, v2) -> {
        try {
          Object field1 = getFieldValue(v1, sortField);
          Object field2 = getFieldValue(v2, sortField);

          if (field1 == null && field2 == null)
            return 0;
          if (field1 == null)
            return ascending ? -1 : 1;
          if (field2 == null)
            return ascending ? 1 : -1;

          int comparison = compareValues(field1, field2);
          return ascending ? comparison : -comparison;
        } catch (Exception e) {
          log.warn("Cache [{}]: Error comparing field {}", cacheName, sortField, e);
          return 0;
        }
      };

      // Using merge sort (Collections.sort uses TimSort which is efficient)
      list.sort(comparator);
      return list;

    } catch (Exception e) {
      log.error("Cache [{}]: Error sorting by field {}", cacheName, sortField, e);
      return list;
    }
  }

  /**
   * Get field value from object using reflection.
   */
  private Object getFieldValue(V object, String fieldPath) throws Exception {
    Object current = object;
    String[] parts = fieldPath.split("\\.");

    for (String part : parts) {
      if (current == null)
        return null;

      Class<?> clazz = current.getClass();

      // Try to get field directly
      try {
        Field field = findField(clazz, part);
        field.setAccessible(true);
        current = field.get(current);
      } catch (NoSuchFieldException e) {
        // Try getter method
        String getterName = "get" + part.substring(0, 1).toUpperCase() + part.substring(1);
        try {
          current = clazz.getMethod(getterName).invoke(current);
        } catch (Exception ex) {
          // Try 'is' prefix for boolean
          getterName = "is" + part.substring(0, 1).toUpperCase() + part.substring(1);
          current = clazz.getMethod(getterName).invoke(current);
        }
      }
    }

    return current;
  }

  /**
   * Find field in class hierarchy.
   */
  private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    while (clazz != null) {
      try {
        return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }

  /**
   * Compare two objects.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private int compareValues(Object o1, Object o2) {
    if (o1 instanceof Comparable && o2 instanceof Comparable) {
      return ((Comparable) o1).compareTo(o2);
    }
    return o1.toString().compareTo(o2.toString());
  }

  /**
   * Paginate a list using efficient subList approach.
   */
  private PageResponse<V> paginateList(List<V> list, int page, int size) {
    long totalElements = list.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);

    // Calculate start and end indices
    int startIndex = page * size;
    int endIndex = Math.min(startIndex + size, list.size());

    // Handle out of bounds
    if (startIndex >= list.size()) {
      return new PageResponse<>(
          new ArrayList<>(),
          page + 1,
          page,
          totalElements,
          totalPages,
          page == 0,
          true);
    }

    // Extract page content using subList (O(1) operation)
    List<V> content = list.subList(startIndex, endIndex);

    return new PageResponse<>(
        new ArrayList<>(content),
        page + 1,
        page,
        totalElements,
        totalPages,
        page == 0,
        endIndex >= list.size());
  }

  /**
   * Get cache statistics.
   */
  public CacheStats getStats() {
    long totalRequests = hits + misses;
    double hitRate = totalRequests > 0 ? (double) hits / totalRequests * 100 : 0.0;
    return new CacheStats(cacheName, cache.size(), ttlMillis, hits, misses, evictions, hitRate);
  }

  /**
   * Reset cache metrics.
   */
  public void resetMetrics() {
    hits = 0;
    misses = 0;
    evictions = 0;
    log.info("Cache [{}]: Metrics reset", cacheName);
  }

  /**
   * Cache statistics record.
   */
  public record CacheStats(
      String name,
      int size,
      long ttlMillis,
      long hits,
      long misses,
      long evictions,
      double hitRate) {
    public long totalRequests() {
      return hits + misses;
    }
  }

}
