package com.enterprise.project_management.dto.issue;

import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.IssuePriority;
import com.enterprise.project_management.entity.IssueStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class IssueResponse {

  private Long id;
  private Long projectId;
  private String projectName;

  private String title;
  private String description;

  private IssuePriority priority;
  private IssueStatus status;

  private Long assignedTo;
  private String assignedToName;
  private String assignedToEmail;

  private Long createdBy;
  private String createdByName;
  private String createdByEmail;

  private LocalDate dueDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean previousAssigneeUnavailable;

  public IssueResponse(
      Long id,
      Long projectId,
      String projectName,
      String title,
      String description,
      IssuePriority priority,
      IssueStatus status,
      Long assignedTo,
      String assignedToName,
      String assignedToEmail,
      Long createdBy,
      String createdByName,
      String createdByEmail,
      LocalDate dueDate,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.projectId = projectId;
    this.projectName = projectName;
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.status = status;
    this.assignedTo = assignedTo;
    this.assignedToName = assignedToName;
    this.assignedToEmail = assignedToEmail;
    this.createdBy = createdBy;
    this.createdByName = createdByName;
    this.createdByEmail = createdByEmail;
    this.dueDate = dueDate;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static IssueResponse from(Issue issue) {

    Long assignedTo = null;
    String assignedToName = null;
    String assignedToEmail = null;

    if (issue.getAssignedTo() != null) {
      assignedTo = issue.getAssignedTo().getId();
      assignedToName = issue.getAssignedTo().getName();
      assignedToEmail = issue.getAssignedTo().getEmail();
    }

    return new IssueResponse(
        issue.getId(),
        issue.getProject().getId(),
        issue.getProject().getName(),
        issue.getTitle(),
        issue.getDescription(),
        issue.getPriority(),
        issue.getStatus(),
        assignedTo,
        assignedToName,
        assignedToEmail,
        issue.getCreatedBy().getId(),
        issue.getCreatedBy().getName(),
        issue.getCreatedBy().getEmail(),
        issue.getDueDate(),
        issue.getCreatedAt(),
        issue.getUpdatedAt());
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public IssuePriority getPriority() {
    return priority;
  }

  public IssueStatus getStatus() {
    return status;
  }

  public Long getAssignedTo() {
    return assignedTo;
  }

  public String getAssignedToName() {
    return assignedToName;
  }

  public String getAssignedToEmail() {
    return assignedToEmail;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public String getCreatedByName() {
    return createdByName;
  }

  public String getCreatedByEmail() {
    return createdByEmail;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public boolean isPreviousAssigneeUnavailable() { return previousAssigneeUnavailable; }

  public void setPreviousAssigneeUnavailable(boolean unavailable) { this.previousAssigneeUnavailable = unavailable; }
}
