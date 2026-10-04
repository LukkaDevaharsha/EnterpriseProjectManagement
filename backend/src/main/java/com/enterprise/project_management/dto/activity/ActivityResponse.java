package com.enterprise.project_management.dto.activity;

import com.enterprise.project_management.entity.Activity;
import java.time.LocalDateTime;

public record ActivityResponse(
    Long id,
    String action,
    String description,
    String actorName,
    Long projectId,
    Long issueId,
    String oldStatus,
    String newStatus,
    LocalDateTime createdAt) {
  public static ActivityResponse from(Activity a) {
    return new ActivityResponse(
        a.getId(),
        a.getAction(),
        a.getDescription(),
        a.getActor().getName(),
        a.getProject() == null ? null : a.getProject().getId(),
        a.getIssue() == null ? null : a.getIssue().getId(),
        a.getOldStatus(),
        a.getNewStatus(),
        a.getCreatedAt());
  }
}
