package com.enterprise.project_management.dto.admin;

import com.enterprise.project_management.entity.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateRoleRequest {

  @NotNull(message = "Role is required")
  private Role role;

  public UpdateRoleRequest() {}

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }
}
