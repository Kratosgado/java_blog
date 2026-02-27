package com.kratosgado.blog.backend.controllers.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class CacheControllerTest {

  @Mock private CacheManager cacheManager;

  @Mock private Cache cache;

  @InjectMocks private CacheController cacheController;

  @Test
  @DisplayName("getCacheNames should wrap cache names in ResponseDto")
  void getCacheNames_shouldReturnNames() {
    when(cacheManager.getCacheNames()).thenReturn(Set.of("posts", "users"));

    java.util.Collection<String> response = cacheController.getCacheNames();

    assertThat(response).containsExactlyInAnyOrder("posts", "users");
  }

  @Test
  @DisplayName("clearCache should clear existing cache and return success")
  void clearCache_shouldClearWhenExists() {
    when(cacheManager.getCache("posts")).thenReturn(cache);

    cacheController.clearCache("posts");

    verify(cache).clear();
  }

  @Test
  @DisplayName("clearAllCaches should clear each cache")
  void clearAllCaches_shouldClearAll() {
    when(cacheManager.getCacheNames()).thenReturn(Set.of("a", "b"));
    when(cacheManager.getCache(any())).thenReturn(cache);
    cacheController.clearAllCaches();

    verify(cache, times(2)).clear();
  }

  @Test
  @DisplayName("evictCacheEntry should evict string and numeric keys when cache exists")
  void evictCacheEntry_shouldEvictKeys() {
    when(cacheManager.getCache("posts")).thenReturn(cache);
    cacheController.evictCacheEntry("posts", "123");

    verify(cache).evict("123");
    verify(cache).evict(123L);
  }

  @Test
  @DisplayName("evictCacheEntry should return bad request when cache missing")
  void evictCacheEntry_whenMissing_shouldReturnBadRequest() {
    when(cacheManager.getCache("missing")).thenReturn(null);
    cacheController.evictCacheEntry("missing", "k");
    verify(cache).evict(any());
  }
}
