package com.enterprise.project_management.dto.project;

import com.enterprise.project_management.entity.Project;
import com.enterprise.project_management.entity.ProjectStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectResponse {

  private Long id;
  private String name;
  private String description;
  private ProjectStatus status;
  private LocalDate startDate;
  private LocalDate endDate;
  private Long managerId;
  private String managerName;
  private String managerEmail;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private int progressPercent;

  public ProjectResponse(
      Long id,
      String name,
      String description,
      ProjectStatus status,
      LocalDate startDate,
      LocalDate endDate,
      Long managerId,
      String managerName,
      String managerEmail,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      int progressPercent) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.status = status;
    this.startDate = startDate;
    this.endDate = endDate;
    this.managerId = managerId;
    this.managerName = managerName;
    this.managerEmail = managerEmail;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.progressPercent = progressPercent;
  }

  public static ProjectResponse from(Project project) {

    Long managerId = null;
    String managerName = null;
    String managerEmail = null;

    if (project.getManager() != null) {
      managerId = project.getManager().getId();
      managerName = project.getManager().getName();
      managerEmail = project.getManager().getEmail();
    }

    return new ProjectResponse(
        project.getId(),
        project.getName(),
        project.getDescription(),
        project.getStatus(),
        project.getStartDate(),
        project.getEndDate(),
        managerId,
        managerName,
        managerEmail,
        project.getCreatedAt(),
        project.getUpdatedAt(),
        0);
  }

  public static ProjectResponse from(Project project, int progressPercent) {
    ProjectResponse response = from(project);
    response.progressPercent = progressPercent;
    return response;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public ProjectStatus getStatus() {
    return status;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Long getManagerId() {
    return managerId;
  }

  public String getManagerName() {
    return managerName;
  }

  public String getManagerEmail() {
    return managerEmail;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public int getProgressPercent() {
    return progressPercent;
  }
}
