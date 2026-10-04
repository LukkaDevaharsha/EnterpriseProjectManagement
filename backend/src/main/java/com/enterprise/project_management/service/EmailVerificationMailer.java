package com.enterprise.project_management.service;

import com.enterprise.project_management.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationMailer {
  private final JavaMailSender mailSender;
  private final String from;

  public EmailVerificationMailer(JavaMailSender mailSender, @Value("${app.mail.from:}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  public void sendCode(User user, String code) {
    if (from.isBlank()) {
      throw new IllegalStateException("Email verification is not configured. Set GMAIL_USERNAME and GMAIL_APP_PASSWORD on the backend.");
    }
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(user.getEmail());
    message.setSubject("Verify your Enterprise Project Management account");
    message.setText("Hello " + user.getName() + ",\n\nYour email verification code is " + code
        + ". It expires in 15 minutes.\n\nIf you did not create this account, you can ignore this email.");
    mailSender.send(message);
  }
}
