package com.kratosgado.blog.backend.cache;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.PageResponse;

/**
 * Generic cache service interface for managing cached entities with support for
 * searching, sorting, and pagination.
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
public interface CacheService<K, V> {

  /**
   * Store a value in the cache with the given key.
   * 
   * @param key   the key
   * @param value the value to cache
   */
  void put(K key, V value);

  /**
   * Update a value in the cache with the given key if present
   * 
   * @param key   the key
   * @param value the new value to update
   */
  void updateIfPresent(K key, V value);

  /**
   * Retrieve a value from the cache.
   * 
   * @param key the key
   * @return an Optional containing the value if present
   */
  Optional<V> get(K key);

  /**
   * Remove a value from the cache.
   * 
   * @param key the key to remove
   */
  void evict(K key);

  /**
   * Clear all entries from the cache.
   */
  void clear();

  /**
   * Get all values from the cache.
   * 
   * @return list of all cached values
   */
  List<V> getAll();

  /**
   * Search the cache using a predicate with pagination and sorting.
   * 
   * @param searchPredicate predicate to filter results
   * @param pageRequest     pagination and sorting parameters
   * @return paginated response with matching results
   */
  PageResponse<V> search(Predicate<V> searchPredicate, PageRequest pageRequest);

  /**
   * Get paginated results from cache.
   * 
   * @param pageRequest pagination and sorting parameters
   * @return paginated response
   */
  PageResponse<V> paginate(PageRequest pageRequest);

  /**
   * Check if a key exists in the cache.
   * 
   * @param key the key
   * @return true if key exists
   */
  boolean containsKey(K key);

  /**
   * Get the size of the cache.
   * 
   * @return number of entries in cache
   */
  int size();

  /**
   * Refresh the cache by clearing and reloading all entries.
   */
  void refresh();
}
