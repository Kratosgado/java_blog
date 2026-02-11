package com.kratosgado.blog.backend.services;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Login Attempt Service - Brute-Force Attack Detection
 *
 * <p>Implements rate limiting and account lockout to prevent brute-force password attacks.
 * Uses in-memory tracking with ConcurrentHashMap for O(1) lookup performance.
 *
 * <p><b>Data Structure & Algorithm Analysis:</b>
 * <ul>
 *   <li><b>Data Structure:</b> ConcurrentHashMap&lt;String, LoginAttemptRecord&gt;</li>
 *   <li><b>Key:</b> Username or IP address (String)</li>
 *   <li><b>Value:</b> LoginAttemptRecord (attempt count, first attempt timestamp)</li>
 *   <li><b>Time Complexity:</b>
 *     <ul>
 *       <li>recordFailedAttempt(): O(1) - HashMap put/get</li>
 *       <li>recordSuccessfulAttempt(): O(1) - HashMap remove</li>
 *       <li>isBlocked(): O(1) - HashMap get + simple timestamp check</li>
 *       <li>cleanupOldRecords(): O(n) - Iterates all entries, runs periodically</li>
 *     </ul>
 *   </li>
 *   <li><b>Space Complexity:</b> O(n) where n = number of users with failed attempts</li>
 * </ul>
 *
 * <p><b>Security Configuration:</b>
 * <ul>
 *   <li>Max Failed Attempts: 5 (configurable via MAX_ATTEMPTS property)</li>
 *   <li>Lockout Duration: 15 minutes (configurable via LOCKOUT_DURATION_MS property)</li>
 *   <li>Cleanup Interval: Every 30 minutes (removes old records)</li>
 * </ul>
 *
 * <p><b>Attack Mitigation:</b>
 * <ul>
 *   <li>Prevents brute-force: Blocks account after N failed attempts</li>
 *   <li>Prevents credential stuffing: Rate limits login attempts</li>
 *   <li>Automatic recovery: Lockout expires after time period</li>
 *   <li>Memory efficient: Periodic cleanup prevents unbounded growth</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b>
 * ConcurrentHashMap ensures thread-safe operations in multi-threaded environment.
 *
 * <p><b>Limitations (In-Memory Storage):</b>
 * <ul>
 *   <li>Not shared across multiple application instances (use Redis for distributed systems)</li>
 *   <li>Lost on application restart (acceptable for short-term rate limiting)</li>
 *   <li>Memory usage grows with number of failed login attempts</li>
 * </ul>
 */
@Slf4j
@Service
public class LoginAttemptService {

  /**
   * Maximum allowed failed login attempts before lockout
   *
   * <p>Default: 5 attempts
   * <p>Configurable via: security.login.max-attempts property
   */
  @Value("${security.login.max-attempts:5}")
  private int MAX_ATTEMPTS;

  /**
   * Lockout duration in milliseconds
   *
   * <p>Default: 900,000 ms (15 minutes)
   * <p>Configurable via: security.login.lockout-duration-ms property
   */
  @Value("${security.login.lockout-duration-ms:900000}")
  private long LOCKOUT_DURATION_MS;

  /**
   * Storage for login attempt records
   *
   * <p>Key: Username (case-insensitive) or IP address
   * <p>Value: LoginAttemptRecord with attempt count and timestamp
   */
  private final ConcurrentHashMap<String, LoginAttemptRecord> attemptCache =
      new ConcurrentHashMap<>();

  /**
   * Record a failed login attempt
   *
   * <p><b>Time Complexity:</b> O(1)
   *
   * <p><b>Algorithm:</b>
   * 1. Normalize username (lowercase for case-insensitive tracking)
   * 2. Get existing record or create new one
   * 3. Increment attempt count
   * 4. If first attempt, record timestamp
   * 5. Check if lockout threshold reached
   *
   * @param username The username that failed authentication
   */
  public void recordFailedAttempt(String username) {
    String key = normalizeKey(username);

    attemptCache.compute(key, (k, existingRecord) -> {
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
          log.warn("⚠ ACCOUNT LOCKED | User: {} | Failed attempts: {} | Lockout duration: {}ms",
              username, newAttemptCount, LOCKOUT_DURATION_MS);
        }

        return new LoginAttemptRecord(newAttemptCount, existingRecord.firstAttemptTimestamp);
      }
    });
  }

  /**
   * Record a successful login attempt (clears failed attempt history)
   *
   * <p><b>Time Complexity:</b> O(1)
   *
   * <p><b>Algorithm:</b>
   * 1. Normalize username
   * 2. Remove record from attempt cache
   * 3. User can login again immediately on next failure
   *
   * <p><b>Design Decision:</b>
   * Successful login resets the attempt counter. This prevents permanent
   * lockout if user eventually enters correct password.
   *
   * @param username The username that successfully authenticated
   */
  public void recordSuccessfulAttempt(String username) {
    String key = normalizeKey(username);
    LoginAttemptRecord removed = attemptCache.remove(key);

    if (removed != null) {
      log.info("✓ Cleared failed login attempts for: {} (had {} failed attempts)",
          username, removed.attemptCount);
    }
  }

  /**
   * Check if user/IP is currently blocked due to too many failed attempts
   *
   * <p><b>Time Complexity:</b> O(1)
   *
   * <p><b>Algorithm:</b>
   * 1. Get attempt record (O(1) HashMap lookup)
   * 2. If no record → return false (not blocked)
   * 3. Check if lockout duration has expired
   * 4. If expired → remove record and return false
   * 5. If not expired and attempts ≥ max → return true (blocked)
   *
   * <p><b>Automatic Unlock:</b>
   * If lockout duration has passed, user is automatically unlocked.
   * This implements time-based recovery without manual intervention.
   *
   * @param username The username to check
   * @return true if user is blocked, false otherwise
   */
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
      log.info("⏱ Lockout expired for: {} after {}ms. Account unlocked.",
          username, timeSinceFirstAttempt);
      return false;
    }

    // Check if max attempts exceeded and still within lockout window
    boolean blocked = record.attemptCount >= MAX_ATTEMPTS;

    if (blocked) {
      long remainingLockoutTime = LOCKOUT_DURATION_MS - timeSinceFirstAttempt;
      log.warn("⊘ LOGIN BLOCKED | User: {} | Attempts: {} | Remaining lockout: {}ms",
          username, record.attemptCount, remainingLockoutTime);
    }

    return blocked;
  }

  /**
   * Get remaining lockout time in milliseconds
   *
   * <p><b>Time Complexity:</b> O(1)
   *
   * @param username The username to check
   * @return Remaining lockout time in milliseconds, or 0 if not blocked
   */
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

  /**
   * Get current attempt count for user
   *
   * <p><b>Time Complexity:</b> O(1)
   *
   * @param username The username to check
   * @return Number of failed attempts, or 0 if no attempts recorded
   */
  public int getAttemptCount(String username) {
    String key = normalizeKey(username);
    LoginAttemptRecord record = attemptCache.get(key);
    return record != null ? record.attemptCount : 0;
  }

  /**
   * Scheduled cleanup task to remove old attempt records
   *
   * <p><b>Time Complexity:</b> O(n) where n = number of tracked users
   *
   * <p><b>Schedule:</b> Runs every 30 minutes
   *
   * <p><b>Algorithm:</b>
   * 1. Iterate all records
   * 2. Check if record is older than lockout duration
   * 3. Remove expired records
   *
   * <p><b>Memory Leak Prevention:</b>
   * Without cleanup, cache would grow unbounded with failed login attempts.
   * This ensures memory usage remains bounded over time.
   */
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
    log.info("Login attempt cleanup completed. Removed {} expired records. " +
        "Before: {} records, After: {} records",
        removedCount, initialSize, finalSize);
  }

  /**
   * Clear all login attempt records (for testing/admin purposes)
   *
   * <p><b>Time Complexity:</b> O(n)
   *
   * <p><b>⚠️ Warning:</b> Use with caution. This will unlock all blocked accounts.
   */
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

  /**
   * Internal record class for tracking login attempts
   *
   * <p><b>Fields:</b>
   * <ul>
   *   <li>attemptCount: Number of failed login attempts</li>
   *   <li>firstAttemptTimestamp: Unix epoch milliseconds of first failed attempt</li>
   * </ul>
   *
   * <p><b>Design Decision:</b>
   * Record class (immutable) is used for thread-safe value storage.
   * Each update creates a new record instance.
   */
  private record LoginAttemptRecord(int attemptCount, long firstAttemptTimestamp) {
  }
}
