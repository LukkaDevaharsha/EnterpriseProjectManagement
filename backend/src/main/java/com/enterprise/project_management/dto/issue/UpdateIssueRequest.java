package com.enterprise.project_management.dto.issue;

import com.enterprise.project_management.entity.IssuePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class UpdateIssueRequest {

  @NotBlank(message = "Issue title is required")
  @Size(max = 200, message = "Title must not exceed 200 characters")
  private String title;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  private String description;

  private IssuePriority priority;

  private Long assignedTo;

  private LocalDate dueDate;

  public UpdateIssueRequest() {}

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public IssuePriority getPriority() {
    return priority;
  }

  public void setPriority(IssuePriority priority) {
    this.priority = priority;
  }

  public Long getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(Long assignedTo) {
    this.assignedTo = assignedTo;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }
}
