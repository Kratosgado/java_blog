package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredGetEndpoint;
import com.kratosgado.blog.dtos.response.ResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "Cache monitoring and management APIs")
public class CacheController {

  private final CacheManager cacheManager;

  @GetMapping("/names")
  @SecuredGetEndpoint(
      summary = "Get cache names",
      description = "Retrieves names of all application caches. Requires admin role.")
  public ResponseEntity<ResponseDto<Collection<String>>> getCacheNames() {
    return ResponseEntity.ok(
        ResponseDto.success("Cache names retrieved successfully", cacheManager.getCacheNames()));
  }

  @DeleteMapping("/{cacheName}")
  @DeleteEndpoint(
      summary = "Clear a cache",
      description = "Clears all entries from the specified cache. Requires admin role.")
  public ResponseEntity<ResponseDto<String>> clearCache(@PathVariable String cacheName) {
    log.info("Clearing cache: {}", cacheName);
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.clear();
      return ResponseEntity.ok(ResponseDto.success("Cache cleared successfully", cacheName));
    }
    return ResponseEntity.badRequest().body(ResponseDto.error("Invalid cache name: " + cacheName));
  }

  @DeleteMapping
  @DeleteEndpoint(
      summary = "Clear all caches",
      description = "Clears all application caches. Requires admin role.")
  public ResponseEntity<ResponseDto<String>> clearAllCaches() {
    log.info("Clearing all caches");
    cacheManager
        .getCacheNames()
        .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    return ResponseEntity.ok(ResponseDto.success("All caches cleared successfully", "all"));
  }

  @DeleteMapping("/{cacheName}/{key}")
  @DeleteEndpoint(
      summary = "Evict cache entry",
      description = "Evicts a specific entry from the cache. Requires admin role.")
  public ResponseEntity<ResponseDto<String>> evictCacheEntry(
      @PathVariable String cacheName, @PathVariable String key) {
    log.info("Evicting entry from cache: {} with key: {}", cacheName, key);
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.evict(key);
      try {
        cache.evict(Long.parseLong(key));
      } catch (NumberFormatException ignored) {
      }
      return ResponseEntity.ok(ResponseDto.success("Cache entry evicted successfully", key));
    }
    return ResponseEntity.badRequest().body(ResponseDto.error("Invalid cache name: " + cacheName));
  }
}
