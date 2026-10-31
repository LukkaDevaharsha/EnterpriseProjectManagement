package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.auth.LoginRequest;
import com.enterprise.project_management.dto.auth.RegisterRequest;
import com.enterprise.project_management.dto.auth.EmailVerificationRequest;
import com.enterprise.project_management.dto.auth.ResendVerificationRequest;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.security.JwtService;
import com.enterprise.project_management.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Locale;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthController(
      UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
    this.userService = userService;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    try {
      User user = userService.register(request);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              Map.of(
                  "message", "Account created. A verification code has been sent to your email.",
                  "id", user.getId(),
                  "name", user.getName(),
                  "email", user.getEmail(),
                  "role", user.getRole()));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (DataIntegrityViolationException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("message", "Email or Employee ID is already registered."));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(Map.of("message", "Account registration could not complete because the verification email could not be sent. Check the Brevo email configuration and try again."));
    }
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleInvalidRequest(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .findFirst().map(error -> error.getDefaultMessage()).orElse("Registration details are invalid.");
    return ResponseEntity.badRequest().body(Map.of("message", message));
  }

  @PostMapping("/verify-email")
  public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
    try {
      userService.verifyEmail(request.getEmail(), request.getCode());
      return ResponseEntity.ok(Map.of("message", "Email verified. You can now sign in."));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/resend-verification")
  public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
    try {
      userService.resendVerificationCode(request.getEmail());
      return ResponseEntity.ok(Map.of("message", "If the account needs verification, a new code has been sent."));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(Map.of("message", "Could not send the verification email. Check the Brevo email configuration and try again."));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    User user = userService.findByEmail(request.getEmail()).orElse(null);
    if (user != null && !user.isEmailVerified()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("message", "Verify your email before signing in."));
    }
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(Locale.ROOT), request.getPassword()));

    user =
        userService
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

    return ResponseEntity.ok(
        Map.of(
            "message", "Login successful",
            "token", token,
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole()));
  }
}
