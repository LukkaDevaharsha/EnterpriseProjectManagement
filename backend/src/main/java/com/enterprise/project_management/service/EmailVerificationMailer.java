package com.enterprise.project_management.service;

import com.enterprise.project_management.entity.User;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationMailer {
  private final BrevoEmailClient emailClient;

  public EmailVerificationMailer(BrevoEmailClient emailClient) {
    this.emailClient = emailClient;
  }

  public void sendCode(User user, String code) {
    String text = "Hello " + user.getName() + ",\n\nYour email verification code is " + code
        + ". It expires in 15 minutes.\n\nIf you did not create this account, you can ignore this email.";
    emailClient.send(user.getEmail(), "Verify your Enterprise Project Management account", text, null);
  }
}
