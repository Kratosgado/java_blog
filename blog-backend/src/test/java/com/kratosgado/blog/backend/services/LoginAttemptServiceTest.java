package com.kratosgado.blog.backend.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

  private LoginAttemptService service;

  @BeforeEach
  void setUp() {
    service = new LoginAttemptService();
  }

  @Test
  @DisplayName("recordFailedAttempt should increase attempt count and eventually block user")
  void recordFailedAttempt_shouldBlockAfterMaxAttempts() {
    // Use reflection to set MAX_ATTEMPTS and LOCKOUT_DURATION_MS for deterministic test
    setField("MAX_ATTEMPTS", 3);
    setField("LOCKOUT_DURATION_MS", 60_000L);

    String username = "user@example.com";

    service.recordFailedAttempt(username);
    service.recordFailedAttempt(username);

    assertThat(service.isBlocked(username)).isFalse();

    service.recordFailedAttempt(username);

    assertThat(service.isBlocked(username)).isTrue();
    assertThat(service.getAttemptCount(username)).isEqualTo(3);
  }

  @Test
  @DisplayName("recordSuccessfulAttempt should clear attempts")
  void recordSuccessfulAttempt_shouldClearAttempts() {
    setField("MAX_ATTEMPTS", 3);
    setField("LOCKOUT_DURATION_MS", 60_000L);
    String username = "user@example.com";

    service.recordFailedAttempt(username);
    assertThat(service.getAttemptCount(username)).isEqualTo(1);

    service.recordSuccessfulAttempt(username);
    assertThat(service.getAttemptCount(username)).isZero();
  }

  @Test
  @DisplayName("getRemainingLockoutTime should be zero when not blocked")
  void getRemainingLockoutTime_whenNotBlocked_shouldReturnZero() {
    setField("MAX_ATTEMPTS", 3);
    setField("LOCKOUT_DURATION_MS", 60_000L);

    assertThat(service.getRemainingLockoutTime("user")).isZero();
  }

  @Test
  @DisplayName("cleanupOldRecords should remove expired entries")
  void cleanupOldRecords_shouldRemoveExpired() throws Exception {
    setField("MAX_ATTEMPTS", 1);
    setField("LOCKOUT_DURATION_MS", 1L);

    String username = "user";
    service.recordFailedAttempt(username);

    // Wait briefly to ensure lockout duration has passed
    Thread.sleep(5L);

    service.cleanupOldRecords();

    assertThat(service.getAttemptCount(username)).isZero();
  }

  @Test
  @DisplayName("clearAllAttempts should remove all records")
  void clearAllAttempts_shouldClear() {
    setField("MAX_ATTEMPTS", 3);
    setField("LOCKOUT_DURATION_MS", 60_000L);
    service.recordFailedAttempt("user1");
    service.recordFailedAttempt("user2");

    service.clearAllAttempts();

    assertThat(service.getAttemptCount("user1")).isZero();
    assertThat(service.getAttemptCount("user2")).isZero();
  }

  @Test
  @DisplayName("isBlocked should unlock after lockout duration expires")
  void isBlocked_afterDuration_shouldUnlock() throws Exception {
    setField("MAX_ATTEMPTS", 1);
    setField("LOCKOUT_DURATION_MS", 5L);

    String username = "user";
    service.recordFailedAttempt(username);
    assertThat(service.isBlocked(username)).isTrue();

    Thread.sleep(10L);

    assertThat(service.isBlocked(username)).isFalse();
  }

  private void setField(String name, Object value) {
    try {
      java.lang.reflect.Field field = LoginAttemptService.class.getDeclaredField(name);
      field.setAccessible(true);
      field.set(service, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
