package com.enterprise.project_management.dto.admin;

import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import java.time.LocalDateTime;

public class AdminUserResponse {

  private Long id;
  private String name;
  private String email;
  private String employeeId;
  private String department;
  private Role role;
  private boolean active;
  private LocalDateTime createdAt;

  public AdminUserResponse() {}

  public static AdminUserResponse from(User user) {

    AdminUserResponse response = new AdminUserResponse();

    response.id = user.getId();
    response.name = user.getName();
    response.email = user.getEmail();
    response.employeeId = user.getEmployeeId();
    response.department = user.getDepartment();
    response.role = user.getRole();
    response.active = user.isActive();
    response.createdAt = user.getCreatedAt();

    return response;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public String getDepartment() {
    return department;
  }

  public Role getRole() {
    return role;
  }

  public boolean isActive() {
    return active;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
