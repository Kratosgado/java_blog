package com.kratosgado.blog.backend.controllers.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kratosgado.blog.dtos.response.ResponseDto;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CacheControllerTest {

  @Mock private CacheManager cacheManager;

  @Mock private Cache cache;

  @InjectMocks private CacheController cacheController;

  @Test
  @DisplayName("getCacheNames should wrap cache names in ResponseDto")
  void getCacheNames_shouldReturnNames() {
    when(cacheManager.getCacheNames()).thenReturn(Set.of("posts", "users"));

    ResponseEntity<ResponseDto<java.util.Collection<String>>> response =
        cacheController.getCacheNames();

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().data()).containsExactlyInAnyOrder("posts", "users");
  }

  @Test
  @DisplayName("clearCache should clear existing cache and return success")
  void clearCache_shouldClearWhenExists() {
    when(cacheManager.getCache("posts")).thenReturn(cache);

    ResponseEntity<ResponseDto<String>> response = cacheController.clearCache("posts");

    verify(cache).clear();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().data()).isEqualTo("posts");
  }

  @Test
  @DisplayName("clearCache should return bad request when cache missing")
  void clearCache_whenMissing_shouldReturnBadRequest() {
    when(cacheManager.getCache("missing")).thenReturn(null);

    ResponseEntity<ResponseDto<String>> response = cacheController.clearCache("missing");

    assertThat(response.getStatusCode().is4xxClientError()).isTrue();
  }

  @Test
  @DisplayName("clearAllCaches should clear each cache")
  void clearAllCaches_shouldClearAll() {
    when(cacheManager.getCacheNames()).thenReturn(Set.of("a", "b"));
    when(cacheManager.getCache(any())).thenReturn(cache);

    ResponseEntity<ResponseDto<String>> response = cacheController.clearAllCaches();

    verify(cache, times(2)).clear();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
  }

  @Test
  @DisplayName("evictCacheEntry should evict string and numeric keys when cache exists")
  void evictCacheEntry_shouldEvictKeys() {
    when(cacheManager.getCache("posts")).thenReturn(cache);

    ResponseEntity<ResponseDto<String>> response = cacheController.evictCacheEntry("posts", "123");

    verify(cache).evict("123");
    verify(cache).evict(123L);
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().data()).isEqualTo("123");
  }

  @Test
  @DisplayName("evictCacheEntry should return bad request when cache missing")
  void evictCacheEntry_whenMissing_shouldReturnBadRequest() {
    when(cacheManager.getCache("missing")).thenReturn(null);

    ResponseEntity<ResponseDto<String>> response = cacheController.evictCacheEntry("missing", "k");

    assertThat(response.getStatusCode().is4xxClientError()).isTrue();
  }
}
