package com.kratosgado.blog.backend.services.impl;

import com.kratosgado.blog.backend.services.*;


import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.backend.utils.BlogConstants.Miliseconds;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
  @Value("${security.login.max-attempts:5}")
  private int MAX_ATTEMPTS;

  @Value("${security.login.lockout-duration-ms:900000}")
  private long LOCKOUT_DURATION_MS;

  /**
   * Storage for login attempt records
   *
   * <p>Key: email (case-insensitive) or IP address
   *
   * <p>Value: LoginAttemptRecord with attempt count and timestamp
   */
  private final ConcurrentHashMap<String, LoginAttemptRecord> attemptCache =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, User> loggedInUsers = new ConcurrentHashMap<>();

  public void recordFailedAttempt(String email) {

    attemptCache.compute(
        email,
        (k, existingRecord) -> {
          if (existingRecord == null) {
            // First failed attempt
            log.debug("Recording first failed login attempt for: {}", email);
            return new LoginAttemptRecord(1, System.currentTimeMillis());
          } else {
            // Subsequent failed attempt
            int newAttemptCount = existingRecord.attemptCount + 1;
            log.debug("Recording failed login attempt #{} for: {}", newAttemptCount, email);

            // Check if lockout threshold reached
            if (newAttemptCount >= MAX_ATTEMPTS) {
              log.warn(
                  "⚠ ACCOUNT LOCKED | User: {} | Failed attempts: {} | Lockout duration: {}ms",
                  email,
                  newAttemptCount,
                  LOCKOUT_DURATION_MS);
            }

            return new LoginAttemptRecord(newAttemptCount, existingRecord.firstAttemptTimestamp);
          }
        });
  }

  public void recordSuccessfulAttempt(User user) {
    LoginAttemptRecord removed = attemptCache.remove(user.getEmail());
    loggedInUsers.put(user.getEmail(), user);

    if (removed != null) {
      log.info(
          "✓ Cleared failed login attempts for: {} (had {} failed attempts)",
          user.getEmail(),
          removed.attemptCount);
    }
  }

  public Optional<User> getLoggedInUser(String email) {
    return Optional.ofNullable(loggedInUsers.get(email));
  }

  public boolean isBlocked(String email) {
    LoginAttemptRecord record = attemptCache.get(email);

    // No failed attempts recorded
    if (record == null) {
      return false;
    }

    // Check if lockout duration has expired
    long currentTime = System.currentTimeMillis();
    long timeSinceFirstAttempt = currentTime - record.firstAttemptTimestamp;

    if (timeSinceFirstAttempt > LOCKOUT_DURATION_MS) {
      // Lockout expired - remove record and allow login
      attemptCache.remove(email);
      log.info(
          "⏱ Lockout expired for: {} after {}ms. Account unlocked.", email, timeSinceFirstAttempt);
      return false;
    }

    // Check if max attempts exceeded and still within lockout window
    boolean blocked = record.attemptCount >= MAX_ATTEMPTS;

    if (blocked) {
      long remainingLockoutTime = LOCKOUT_DURATION_MS - timeSinceFirstAttempt;
      log.warn(
          "⊘ LOGIN BLOCKED | User: {} | Attempts: {} | Remaining lockout: {}ms",
          email,
          record.attemptCount,
          remainingLockoutTime);
    }

    return blocked;
  }

  public long getRemainingLockoutTime(String email) {
    LoginAttemptRecord record = attemptCache.get(email);

    if (record == null || record.attemptCount < MAX_ATTEMPTS) {
      return 0;
    }

    long currentTime = System.currentTimeMillis();
    long timeSinceFirstAttempt = currentTime - record.firstAttemptTimestamp;
    long remainingTime = LOCKOUT_DURATION_MS - timeSinceFirstAttempt;

    return Math.max(0, remainingTime);
  }

  public int getAttemptCount(String email) {
    LoginAttemptRecord record = attemptCache.get(email);
    return record != null ? record.attemptCount : 0;
  }

  @Scheduled(fixedRate = Miliseconds.SIX_HOURS) // Run every 30 minutes
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

  private record LoginAttemptRecord(int attemptCount, long firstAttemptTimestamp) {}
}
