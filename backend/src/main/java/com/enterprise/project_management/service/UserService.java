package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.admin.AdminUserResponse;
import com.enterprise.project_management.dto.auth.RegisterRequest;
import com.enterprise.project_management.dto.user.*;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailVerificationMailer verificationMailer;
  private final SecureRandom secureRandom = new SecureRandom();

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailVerificationMailer verificationMailer) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.verificationMailer = verificationMailer;
  }

  @Transactional
  public User register(RegisterRequest request) {

    String email = normalizeEmail(request.getEmail());

    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already registered");
    }

    String employeeId = request.getEmployeeId().trim();
    if (userRepository.existsByEmployeeId(employeeId)) {
      throw new IllegalArgumentException("Employee ID already registered");
    }

    User user = new User();

    user.setName(request.getName().trim());
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEmployeeId(employeeId);
    user.setDepartment(request.getDepartment().trim());

    // Registration can only create employees.
    user.setRole(Role.EMPLOYEE);

    // New users are active by default.
    user.setActive(true);
    user.setEmailVerified(false);

    User saved = userRepository.save(user);
    issueVerificationCode(saved, LocalDateTime.now());
    return saved;
  }

  public void verifyEmail(String email, String code) {
    User user = userRepository.findByEmail(normalizeEmail(email))
        .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code"));
    if (user.isEmailVerified()) return;
    LocalDateTime now = LocalDateTime.now();
    if (user.getVerificationCodeHash() == null || user.getVerificationExpiresAt() == null
        || user.getVerificationExpiresAt().isBefore(now) || user.getVerificationAttempts() >= 5) {
      clearVerificationCode(user);
      userRepository.save(user);
      throw new IllegalArgumentException("Verification code expired. Request a new code.");
    }
    if (!passwordEncoder.matches(code, user.getVerificationCodeHash())) {
      user.setVerificationAttempts(user.getVerificationAttempts() + 1);
      if (user.getVerificationAttempts() >= 5) clearVerificationCode(user);
      userRepository.save(user);
      throw new IllegalArgumentException("Incorrect verification code");
    }
    user.setEmailVerified(true);
    clearVerificationCode(user);
    userRepository.save(user);
  }

  @Transactional
  public void resendVerificationCode(String email) {
    userRepository.findByEmail(normalizeEmail(email)).ifPresent(user -> {
      if (user.isEmailVerified()) return;
      LocalDateTime now = LocalDateTime.now();
      if (user.getVerificationSentAt() != null && user.getVerificationSentAt().isAfter(now.minusSeconds(60))) {
        throw new IllegalArgumentException("Please wait a minute before requesting another code");
      }
      issueVerificationCode(user, now);
    });
  }

  private void issueVerificationCode(User user, LocalDateTime now) {
    String code = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
    user.setVerificationCodeHash(passwordEncoder.encode(code));
    user.setVerificationExpiresAt(now.plusMinutes(15));
    user.setVerificationSentAt(now);
    user.setVerificationAttempts(0);
    userRepository.save(user);
    verificationMailer.sendCode(user, code);
  }

  private void clearVerificationCode(User user) {
    user.setVerificationCodeHash(null);
    user.setVerificationExpiresAt(null);
    user.setVerificationSentAt(null);
    user.setVerificationAttempts(0);
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(normalizeEmail(email));
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(normalizeEmail(email));
  }

  public List<AdminUserResponse> getEmployees() {
    return userRepository.findAll().stream()
        .filter(user -> user.getRole() == Role.EMPLOYEE)
        .map(AdminUserResponse::from)
        .toList();
  }

  public UserProfileResponse getProfile(String email) {
    return UserProfileResponse.from(
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found")));
  }

  public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setName(request.getName());
    user.setDepartment(request.getDepartment());
    return UserProfileResponse.from(userRepository.save(user));
  }

  public void changePassword(String email, ChangePasswordRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
      throw new IllegalArgumentException("Current password is incorrect");
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }
}
