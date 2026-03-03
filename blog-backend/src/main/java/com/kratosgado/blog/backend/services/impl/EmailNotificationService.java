package com.kratosgado.blog.backend.services.impl;

import com.kratosgado.blog.backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

  private final JavaMailSender mailSender;

  @Async("emailTaskExecutor")
  @Override
  public void sendPasswordResetNotification(String email, String resetToken) {
    log.info("Sending password reset email to user: {}", email);
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(email);
      message.setSubject("Password Reset Request");
      message.setText("To reset your password, please use the following token: " + resetToken);

      mailSender.send(message);
      log.info("Password reset email sent successfully.");
    } catch (Exception e) {
      log.error("Failed to send password reset email", e);
    }
  }
}
