package com.enterprise.project_management.dto.project;

import com.enterprise.project_management.entity.ProjectStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class UpdateProjectRequest {
  @Size(min = 1, max = 150)
  private String name;

  @Size(max = 1000)
  private String description;

  private LocalDate startDate;
  private LocalDate endDate;
  private ProjectStatus status;
  private Long managerId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public ProjectStatus getStatus() {
    return status;
  }

  public void setStatus(ProjectStatus status) {
    this.status = status;
  }

  public Long getManagerId() {
    return managerId;
  }

  public void setManagerId(Long managerId) {
    this.managerId = managerId;
  }
}
