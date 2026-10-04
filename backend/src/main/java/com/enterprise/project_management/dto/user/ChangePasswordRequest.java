package com.enterprise.project_management.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
  @NotBlank private String currentPassword;

  @NotBlank
  @Size(min = 8)
  private String newPassword;

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String value) {
    currentPassword = value;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String value) {
    newPassword = value;
  }
}
