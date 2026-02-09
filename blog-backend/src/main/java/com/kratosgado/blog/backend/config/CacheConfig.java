package com.kratosgado.blog.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.kratosgado.blog.backend.utils.BlogConstants.CacheNames;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();

    // TODO: use reasonable ttls
    List<CaffeineCache> caches =
        Arrays.asList(
            buildCache(CacheNames.POSTS, 10, TimeUnit.DAYS, 1000),
            buildCache(CacheNames.POSTLIST, 1, TimeUnit.DAYS, 200),
            buildCache(CacheNames.TAGS, 1, TimeUnit.HOURS, 500),
            buildCache(CacheNames.TAGLIST, 1, TimeUnit.HOURS, 500),
            buildCache(CacheNames.CATEGORIES, 2, TimeUnit.HOURS, 100),
            buildCache(CacheNames.CATEGORYLIST, 2, TimeUnit.HOURS, 100),
            buildCache(CacheNames.USERS, 1, TimeUnit.HOURS, 100),
            buildCache(CacheNames.USERLIST, 1, TimeUnit.HOURS, 100),
            buildCache(CacheNames.COMMENTS, 1, TimeUnit.HOURS, 100),
            buildCache(CacheNames.COMMENTLIST, 1, TimeUnit.HOURS, 100));

    cacheManager.setCaches(caches);
    return cacheManager;
  }

  private CaffeineCache buildCache(String name, long ttl, TimeUnit timeUnit, long maxSize) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .expireAfterWrite(ttl, timeUnit)
            .maximumSize(maxSize)
            .recordStats()
            .build());
  }
}

