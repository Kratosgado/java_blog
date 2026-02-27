package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.utils.BlogConstants.Miliseconds;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenBlacklistService {

  private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

  public void blacklistToken(String token, long expiryTimestamp) {
    blacklist.put(token, expiryTimestamp);
    log.info("Token blacklisted. Total blacklisted tokens: {}", blacklist.size());
    log.debug(
        "Blacklisted token (first 20 chars): {}..., expires at: {}",
        token.substring(0, Math.min(20, token.length())),
        Instant.ofEpochMilli(expiryTimestamp));
  }

  public boolean isBlacklisted(String token) {
    Long expiryTimestamp = blacklist.get(token);

    // Token not in blacklist
    if (expiryTimestamp == null) {
      return false;
    }

    // Check if token has expired
    long currentTime = System.currentTimeMillis();
    if (currentTime > expiryTimestamp) {
      // Lazy expiry: remove expired token on access
      blacklist.remove(token);
      log.debug("Removed expired token from blacklist on access check");
      return false;
    }

    // Token is blacklisted and still valid
    log.debug("Token is blacklisted (not expired)");
    return true;
  }

  public int getBlacklistSize() {
    return blacklist.size();
  }

  @Scheduled(fixedRate = Miliseconds.ONE_HOUR) // Run every hour
  public void cleanupExpiredTokens() {
    log.info("Starting scheduled blacklist cleanup...");
    long currentTime = System.currentTimeMillis();
    int initialSize = blacklist.size();
    int removedCount = 0;

    // Iterate and remove expired entries
    // ConcurrentHashMap.entrySet() supports safe removal during iteration
    var iterator = blacklist.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      if (currentTime > entry.getValue()) {
        iterator.remove();
        removedCount++;
      }
    }

    int finalSize = blacklist.size();
    log.info(
        "Blacklist cleanup completed. Removed {} expired tokens. "
            + "Before: {} tokens, After: {} tokens",
        removedCount,
        initialSize,
        finalSize);
  }

  public void clearBlacklist() {
    int size = blacklist.size();
    blacklist.clear();
    log.warn(
        "Blacklist cleared! Removed {} tokens. All previously blacklisted tokens "
            + "are now valid again.",
        size);
  }
}
