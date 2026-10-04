package com.enterprise.project_management.dto.project;

import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.IssuePriority;
import com.enterprise.project_management.entity.IssueStatus;
import java.time.LocalDate;

public record ProjectWorkflowTaskResponse(
    Long id,
    int stepNumber,
    String title,
    String description,
    IssuePriority priority,
    IssueStatus status,
    Long assignedTo,
    String assignedToName,
    LocalDate dueDate) {
  public static ProjectWorkflowTaskResponse from(Issue issue, int stepNumber) {
    return new ProjectWorkflowTaskResponse(
        issue.getId(),
        stepNumber,
        issue.getTitle(),
        issue.getDescription(),
        issue.getPriority(),
        issue.getStatus(),
        issue.getAssignedTo() == null ? null : issue.getAssignedTo().getId(),
        issue.getAssignedTo() == null ? null : issue.getAssignedTo().getName(),
        issue.getDueDate());
  }
}
