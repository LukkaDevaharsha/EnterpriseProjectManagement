package com.enterprise.project_management.dto.notification;

import com.enterprise.project_management.entity.Notification;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String title,
    String message,
    String actorName,
    Long projectId,
    Long issueId,
    String issueTitle,
    boolean read,
    LocalDateTime createdAt) {
  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.getId(), notification.getTitle(), notification.getMessage(),
        notification.getActor().getName(),
        notification.getProject() == null ? null : notification.getProject().getId(),
        notification.getIssue().getId(), notification.getIssue().getTitle(),
        notification.getReadAt() != null, notification.getCreatedAt());
  }
}
