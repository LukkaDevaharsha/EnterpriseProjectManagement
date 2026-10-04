package com.enterprise.project_management.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateProjectRequest {

  @NotBlank(message = "Project name is required")
  @Size(max = 150, message = "Project name must not exceed 150 characters")
  private String name;

  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  private String description;

  private LocalDate startDate;

  private LocalDate endDate;

  private Long managerId;

  @Valid
  @Size(max = 50, message = "A project can start with up to 50 steps")
  private List<CreateProjectTaskRequest> tasks = new ArrayList<>();

  public CreateProjectRequest() {}

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

  public Long getManagerId() {
    return managerId;
  }

  public void setManagerId(Long managerId) {
    this.managerId = managerId;
  }

  public List<CreateProjectTaskRequest> getTasks() { return tasks; }

  public void setTasks(List<CreateProjectTaskRequest> tasks) { this.tasks = tasks; }
}
