package com.kratosgado.blog.backend.services;

public interface LoginAttemptService {

  void recordFailedAttempt(String username);

  void recordSuccessfulAttempt(String username);

  boolean isBlocked(String username);

  long getRemainingLockoutTime(String username);

  int getAttemptCount(String username);

  void cleanupOldRecords();

  void clearAllAttempts();
}
