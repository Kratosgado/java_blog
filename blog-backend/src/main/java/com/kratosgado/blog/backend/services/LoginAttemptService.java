package com.kratosgado.blog.backend.services;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginAttemptService {
  @Value("${security.login.max-attempts:5}")
  private int MAX_ATTEMPTS;

  @Value("${security.login.lockout-duration-ms:900000}")
  private long LOCKOUT_DURATION_MS;

  /**
   * Storage for login attempt records
   *
   * <p>Key: Username (case-insensitive) or IP address
   *
   * <p>Value: LoginAttemptRecord with attempt count and timestamp
   */
  private final ConcurrentHashMap<String, LoginAttemptRecord> attemptCache =
      new ConcurrentHashMap<>();

  public void recordFailedAttempt(String username) {
    String key = normalizeKey(username);

    attemptCache.compute(
        key,
        (k, existingRecord) -> {
          if (existingRecord == null) {
            // First failed attempt
            log.debug("Recording first failed login attempt for: {}", username);
            return new LoginAttemptRecord(1, System.currentTimeMillis());
          } else {
            // Subsequent failed attempt
            int newAttemptCount = existingRecord.attemptCount + 1;
            log.debug("Recording failed login attempt #{} for: {}", newAttemptCount, username);

            // Check if lockout threshold reached
            if (newAttemptCount >= MAX_ATTEMPTS) {
              log.warn(
                  "⚠ ACCOUNT LOCKED | User: {} | Failed attempts: {} | Lockout duration: {}ms",
                  username,
                  newAttemptCount,
                  LOCKOUT_DURATION_MS);
            }

            return new LoginAttemptRecord(newAttemptCount, existingRecord.firstAttemptTimestamp);
          }
        });
  }

  public void recordSuccessfulAttempt(String username) {
    String key = normalizeKey(username);
    LoginAttemptRecord removed = attemptCache.remove(key);

    if (removed != null) {
      log.info(
          "✓ Cleared failed login attempts for: {} (had {} failed attempts)",
          username,
          removed.attemptCount);
    }
  }

  public boolean isBlocked(String username) {
    String key = normalizeKey(username);
    LoginAttemptRecord record = attemptCache.get(key);

    // No failed attempts recorded
    if (record == null) {
      return false;
    }

    // Check if lockout duration has expired
    long currentTime = System.currentTimeMillis();
    long timeSinceFirstAttempt = currentTime - record.firstAttemptTimestamp;

    if (timeSinceFirstAttempt > LOCKOUT_DURATION_MS) {
      // Lockout expired - remove record and allow login
      attemptCache.remove(key);
      log.info(
          "⏱ Lockout expired for: {} after {}ms. Account unlocked.",
          username,
          timeSinceFirstAttempt);
      return false;
    }

    // Check if max attempts exceeded and still within lockout window
    boolean blocked = record.attemptCount >= MAX_ATTEMPTS;

    if (blocked) {
      long remainingLockoutTime = LOCKOUT_DURATION_MS - timeSinceFirstAttempt;
      log.warn(
          "⊘ LOGIN BLOCKED | User: {} | Attempts: {} | Remaining lockout: {}ms",
          username,
          record.attemptCount,
          remainingLockoutTime);
    }

    return blocked;
  }

  public long getRemainingLockoutTime(String username) {
    String key = normalizeKey(username);
    LoginAttemptRecord record = attemptCache.get(key);

    if (record == null || record.attemptCount < MAX_ATTEMPTS) {
      return 0;
    }

    long currentTime = System.currentTimeMillis();
    long timeSinceFirstAttempt = currentTime - record.firstAttemptTimestamp;
    long remainingTime = LOCKOUT_DURATION_MS - timeSinceFirstAttempt;

    return Math.max(0, remainingTime);
  }

  public int getAttemptCount(String username) {
    String key = normalizeKey(username);
    LoginAttemptRecord record = attemptCache.get(key);
    return record != null ? record.attemptCount : 0;
  }

  @Scheduled(fixedRate = 1800000) // Run every 30 minutes
  public void cleanupOldRecords() {
    log.info("Starting login attempt cache cleanup...");
    long currentTime = System.currentTimeMillis();
    int initialSize = attemptCache.size();
    int removedCount = 0;

    var iterator = attemptCache.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      long timeSinceFirstAttempt = currentTime - entry.getValue().firstAttemptTimestamp;

      // Remove records older than lockout duration
      if (timeSinceFirstAttempt > LOCKOUT_DURATION_MS) {
        iterator.remove();
        removedCount++;
      }
    }

    int finalSize = attemptCache.size();
    log.info(
        "Login attempt cleanup completed. Removed {} expired records. "
            + "Before: {} records, After: {} records",
        removedCount,
        initialSize,
        finalSize);
  }

  public void clearAllAttempts() {
    int size = attemptCache.size();
    attemptCache.clear();
    log.warn("⚠ All login attempt records cleared! Unlocked {} accounts.", size);
  }

  /**
   * Normalize key for case-insensitive username tracking
   *
   * @param key Username or IP address
   * @return Normalized lowercase key
   */
  private String normalizeKey(String key) {
    return key != null ? key.toLowerCase().trim() : "";
  }

  private record LoginAttemptRecord(int attemptCount, long firstAttemptTimestamp) {}
}
