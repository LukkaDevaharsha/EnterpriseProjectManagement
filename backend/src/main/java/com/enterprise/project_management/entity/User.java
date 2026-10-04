package com.enterprise.project_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "employee_id", nullable = false, unique = true, length = 50)
  private String employeeId;

  @Column(nullable = false, length = 100)
  private String department;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified;

  @Column(name = "verification_code_hash", length = 100)
  private String verificationCodeHash;

  @Column(name = "verification_expires_at")
  private LocalDateTime verificationExpiresAt;

  @Column(name = "verification_sent_at")
  private LocalDateTime verificationSentAt;

  @Column(name = "verification_attempts", nullable = false)
  private int verificationAttempts;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isEmailVerified() { return emailVerified; }

  public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

  public String getVerificationCodeHash() { return verificationCodeHash; }

  public void setVerificationCodeHash(String verificationCodeHash) { this.verificationCodeHash = verificationCodeHash; }

  public LocalDateTime getVerificationExpiresAt() { return verificationExpiresAt; }

  public void setVerificationExpiresAt(LocalDateTime verificationExpiresAt) { this.verificationExpiresAt = verificationExpiresAt; }

  public LocalDateTime getVerificationSentAt() { return verificationSentAt; }

  public void setVerificationSentAt(LocalDateTime verificationSentAt) { this.verificationSentAt = verificationSentAt; }

  public int getVerificationAttempts() { return verificationAttempts; }

  public void setVerificationAttempts(int verificationAttempts) { this.verificationAttempts = verificationAttempts; }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
