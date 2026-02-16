package com.kratosgado.blog.backend.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

  private TokenBlacklistService service;

  @BeforeEach
  void setUp() {
    service = new TokenBlacklistService();
  }

  @Test
  @DisplayName("blacklistToken should mark token as blacklisted until expiry")
  void blacklistToken_shouldAddToken() {
    long future = System.currentTimeMillis() + 1_000L;
    service.blacklistToken("token", future);

    assertThat(service.isBlacklisted("token")).isTrue();
    assertThat(service.getBlacklistSize()).isEqualTo(1);
  }

  @Test
  @DisplayName("isBlacklisted should lazily remove expired tokens")
  void isBlacklisted_shouldRemoveExpiredOnAccess() throws Exception {
    long past = System.currentTimeMillis() - 1_000L;
    service.blacklistToken("expired", past);

    assertThat(service.isBlacklisted("expired")).isFalse();
    assertThat(service.getBlacklistSize()).isZero();
  }

  @Test
  @DisplayName("cleanupExpiredTokens should remove old entries")
  void cleanupExpiredTokens_shouldRemoveExpired() throws Exception {
    long past = System.currentTimeMillis() - 1_000L;
    service.blacklistToken("expired1", past);
    service.blacklistToken("expired2", past);

    service.cleanupExpiredTokens();

    assertThat(service.getBlacklistSize()).isZero();
  }

  @Test
  @DisplayName("clearBlacklist should clear all tokens")
  void clearBlacklist_shouldClearAll() {
    long future = System.currentTimeMillis() + 1_000L;
    service.blacklistToken("a", future);
    service.blacklistToken("b", future);

    service.clearBlacklist();

    assertThat(service.getBlacklistSize()).isZero();
  }
}
