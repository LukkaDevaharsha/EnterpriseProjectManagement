package com.enterprise.project_management.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailVerificationRequest {
  @NotBlank @Email private String email;
  @NotBlank @Pattern(regexp = "\\d{6}", message = "Enter the 6-digit code") private String code;

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
}
