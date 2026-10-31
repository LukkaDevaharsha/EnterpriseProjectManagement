package com.enterprise.project_management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.enterprise.project_management.dto.auth.RegisterRequest;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock EmailVerificationMailer verificationMailer;
  @InjectMocks UserService userService;

  @Test
  void validRegistrationCreatesUnverifiedEmployeeAndStartsEmailVerification() {
    when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
    RegisterRequest request = request();

    User created = userService.register(request);

    assertEquals("person@example.com", created.getEmail());
    assertEquals(Role.EMPLOYEE, created.getRole());
    assertFalse(created.isEmailVerified());
    assertEquals("encoded-password", created.getVerificationCodeHash());
    assertTrue(created.getVerificationExpiresAt().isAfter(LocalDateTime.now()));
    verify(verificationMailer).sendCode(eq(created), anyString());
  }

  @Test
  void duplicateEmailIsRejectedBeforeSaving() {
    when(userRepository.existsByEmail("person@example.com")).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> userService.register(request()));
    verify(userRepository, never()).save(any());
  }

  @Test
  void duplicateEmployeeIdIsRejectedBeforeSaving() {
    when(userRepository.existsByEmployeeId("EMP-900")).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> userService.register(request()));
    verify(userRepository, never()).save(any());
  }

  @Test
  void emailDeliveryFailureIsReportedAndRegistrationDoesNotReturnSuccess() {
    when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
    doThrow(new IllegalStateException("Transactional email is not configured: BREVO_API_KEY is missing.")).when(verificationMailer)
        .sendCode(any(User.class), anyString());

    IllegalStateException error = assertThrows(IllegalStateException.class,
        () -> userService.register(request()));
    assertTrue(error.getMessage().contains("BREVO_API_KEY"));
  }

  @Test
  void expiredVerificationCodeIsClearedAndRejected() {
    User user = pendingUser();
    user.setVerificationExpiresAt(LocalDateTime.now().minusSeconds(1));
    when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    assertThrows(IllegalArgumentException.class,
        () -> userService.verifyEmail("person@example.com", "123456"));
    assertNull(user.getVerificationCodeHash());
    verify(userRepository).save(user);
  }

  @Test
  void fifthIncorrectVerificationAttemptClearsTheCode() {
    User user = pendingUser();
    user.setVerificationAttempts(4);
    user.setVerificationExpiresAt(LocalDateTime.now().plusMinutes(5));
    when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("123456", "hashed-code")).thenReturn(false);
    when(userRepository.save(user)).thenReturn(user);

    assertThrows(IllegalArgumentException.class,
        () -> userService.verifyEmail("person@example.com", "123456"));
    assertNull(user.getVerificationCodeHash());
    assertEquals(0, user.getVerificationAttempts());
  }

  @Test
  void resendIsRateLimitedForSixtySeconds() {
    User user = pendingUser();
    user.setVerificationSentAt(LocalDateTime.now().minusSeconds(20));
    when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));

    assertThrows(IllegalArgumentException.class,
        () -> userService.resendVerificationCode("person@example.com"));
    verify(verificationMailer, never()).sendCode(any(User.class), anyString());
  }

  @Test
  void resendIssuesAndEmailsANewSixDigitCodeAfterRateLimit() {
    User user = pendingUser();
    user.setVerificationSentAt(LocalDateTime.now().minusSeconds(61));
    when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);
    when(passwordEncoder.encode(anyString())).thenReturn("new-hash");

    userService.resendVerificationCode("person@example.com");

    assertEquals("new-hash", user.getVerificationCodeHash());
    assertEquals(0, user.getVerificationAttempts());
    verify(verificationMailer).sendCode(eq(user), matches("\\d{6}"));
  }

  private RegisterRequest request() {
    RegisterRequest request = new RegisterRequest();
    request.setName("Example Person");
    request.setEmail("Person@Example.com");
    request.setPassword("StrongPassword1!");
    request.setEmployeeId("EMP-900");
    request.setDepartment("Engineering");
    return request;
  }

  private User pendingUser() {
    User user = new User();
    user.setEmail("person@example.com");
    user.setRole(Role.EMPLOYEE);
    user.setActive(true);
    user.setEmailVerified(false);
    user.setVerificationCodeHash("hashed-code");
    user.setVerificationAttempts(0);
    return user;
  }
}
