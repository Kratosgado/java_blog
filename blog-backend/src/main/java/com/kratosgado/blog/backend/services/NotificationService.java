package com.kratosgado.blog.backend.services;

public interface NotificationService {
  void sendPasswordResetNotification(String email, String token);
}
