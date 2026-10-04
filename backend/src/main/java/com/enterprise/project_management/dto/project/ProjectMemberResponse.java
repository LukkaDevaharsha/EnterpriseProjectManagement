package com.enterprise.project_management.dto.project;

import com.enterprise.project_management.entity.ProjectMember;

public class ProjectMemberResponse {

  private Long id;
  private Long userId;
  private String name;
  private String email;
  private String employeeId;
  private String department;
  private String role;
  private boolean active;

  public ProjectMemberResponse() {}

  public static ProjectMemberResponse from(ProjectMember member) {

    ProjectMemberResponse response = new ProjectMemberResponse();

    response.id = member.getId();

    if (member.getUser() != null) {
      response.userId = member.getUser().getId();
      response.name = member.getUser().getName();
      response.email = member.getUser().getEmail();
      response.employeeId = member.getUser().getEmployeeId();
      response.department = member.getUser().getDepartment();
      response.role = member.getUser().getRole().name();
      response.active = member.getUser().isActive();
    }

    return response;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
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

  public String getRole() {
    return role;
  }

  public boolean isActive() { return active; }
}
