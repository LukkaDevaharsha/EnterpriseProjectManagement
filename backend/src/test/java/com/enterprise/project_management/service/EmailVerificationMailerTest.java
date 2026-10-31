package com.enterprise.project_management.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.enterprise.project_management.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailVerificationMailerTest {
  @Test
  void missingUsernameOrAppPasswordFailsBeforeSending() {
    JavaMailSender sender = mock(JavaMailSender.class);
    EmailVerificationMailer mailer = new EmailVerificationMailer(sender, "sender@example.com", "sender@example.com", "");

    assertThrows(IllegalStateException.class, () -> mailer.sendCode(new User(), "123456"));
    verifyNoInteractions(sender);
  }

  @Test
  void sendsVerificationCodeToTheRegisteredAddress() {
    JavaMailSender sender = mock(JavaMailSender.class);
    EmailVerificationMailer mailer = new EmailVerificationMailer(sender, "sender@example.com", "sender@example.com", "app-password");
    User user = new User();
    user.setName("Test User");
    user.setEmail("recipient@example.com");

    mailer.sendCode(user, "123456");

    verify(sender).send(argThat((SimpleMailMessage message) ->
        "recipient@example.com".equals(message.getTo()[0])
            && message.getText().contains("123456")));
  }

  @Test
  void propagatesSmtpFailureToTheRegistrationService() {
    JavaMailSender sender = mock(JavaMailSender.class);
    doThrow(new MailSendException("SMTP unavailable")).when(sender).send(any(SimpleMailMessage.class));
    EmailVerificationMailer mailer = new EmailVerificationMailer(sender, "sender@example.com", "sender@example.com", "app-password");
    User user = new User();
    user.setEmail("recipient@example.com");

    assertThrows(MailSendException.class, () -> mailer.sendCode(user, "123456"));
  }
}
