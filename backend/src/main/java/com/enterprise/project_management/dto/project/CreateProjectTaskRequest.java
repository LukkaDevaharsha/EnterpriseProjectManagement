package com.enterprise.project_management.dto.project;

import com.enterprise.project_management.entity.IssuePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CreateProjectTaskRequest {
  @NotBlank(message = "Every project step needs a title")
  @Size(max = 200, message = "Task title must not exceed 200 characters")
  private String title;

  @Size(max = 2000, message = "Task description must not exceed 2000 characters")
  private String description;

  private IssuePriority priority = IssuePriority.MEDIUM;

  private LocalDate dueDate;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public IssuePriority getPriority() { return priority; }
  public void setPriority(IssuePriority priority) { this.priority = priority; }
  public LocalDate getDueDate() { return dueDate; }
  public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
