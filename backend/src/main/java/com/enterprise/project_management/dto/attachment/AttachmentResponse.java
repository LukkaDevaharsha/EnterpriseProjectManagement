package com.enterprise.project_management.dto.attachment;

import com.enterprise.project_management.entity.Attachment;
import java.time.LocalDateTime;

public record AttachmentResponse(
    Long id,
    String originalName,
    String contentType,
    long fileSize,
    String uploadedBy,
    LocalDateTime createdAt) {
  public static AttachmentResponse from(Attachment a) {
    return new AttachmentResponse(
        a.getId(),
        a.getOriginalName(),
        a.getContentType(),
        a.getFileSize(),
        a.getUploadedBy().getName(),
        a.getCreatedAt());
  }
}
