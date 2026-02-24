package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.models.User;
import java.util.Optional;

public interface LoginAttemptService {

  void recordFailedAttempt(String email);

  void recordSuccessfulAttempt(User user);

  Optional<User> getLoggedInUser(String email);

  boolean isBlocked(String email);

  long getRemainingLockoutTime(String email);

  int getAttemptCount(String email);

  void cleanupOldRecords();

  void clearAllAttempts();
}
