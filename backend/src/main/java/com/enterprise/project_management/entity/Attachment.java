package com.enterprise.project_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "issue_id", nullable = false)
  private Issue issue;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "uploaded_by", nullable = false)
  private User uploadedBy;

  @Column(name = "original_name", nullable = false)
  private String originalName;

  @Column(name = "stored_name", nullable = false)
  private String storedName;

  @Column(name = "content_type", length = 150)
  private String contentType;

  @Column(name = "file_size", nullable = false)
  private long fileSize;

  @Column(name = "file_path", nullable = false, length = 1000)
  private String filePath;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  protected Attachment() {}

  public Attachment(
      Issue issue,
      User uploadedBy,
      String originalName,
      String storedName,
      String contentType,
      long fileSize,
      String filePath) {
    this.issue = issue;
    this.uploadedBy = uploadedBy;
    this.originalName = originalName;
    this.storedName = storedName;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.filePath = filePath;
  }

  public Long getId() {
    return id;
  }

  public Issue getIssue() {
    return issue;
  }

  public User getUploadedBy() {
    return uploadedBy;
  }

  public String getOriginalName() {
    return originalName;
  }

  public String getStoredName() {
    return storedName;
  }

  public String getContentType() {
    return contentType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getFilePath() {
    return filePath;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
