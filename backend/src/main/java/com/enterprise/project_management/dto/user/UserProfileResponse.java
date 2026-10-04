package com.enterprise.project_management.dto.user;

import com.enterprise.project_management.entity.User;
import java.time.LocalDateTime;

public record UserProfileResponse(
    Long id,
    String name,
    String email,
    String employeeId,
    String department,
    String role,
    boolean active,
    LocalDateTime createdAt) {
  public static UserProfileResponse from(User u) {
    return new UserProfileResponse(
        u.getId(),
        u.getName(),
        u.getEmail(),
        u.getEmployeeId(),
        u.getDepartment(),
        u.getRole().name(),
        u.isActive(),
        u.getCreatedAt());
  }
}
