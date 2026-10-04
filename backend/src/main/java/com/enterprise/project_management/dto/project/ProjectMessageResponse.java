package com.enterprise.project_management.dto.project;

import com.enterprise.project_management.entity.ProjectMessage;
import java.time.LocalDateTime;

public record ProjectMessageResponse(Long id, Long senderId, String senderName, String content, LocalDateTime createdAt) {
  public static ProjectMessageResponse from(ProjectMessage message) {
    return new ProjectMessageResponse(message.getId(), message.getSender().getId(), message.getSender().getName(), message.getContent(), message.getCreatedAt());
  }
}
