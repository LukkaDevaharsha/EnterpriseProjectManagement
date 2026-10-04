package com.enterprise.project_management.dto.issue;

import com.enterprise.project_management.entity.IssueComment;
import java.time.LocalDateTime;

public class CommentResponse {

  private Long id;
  private Long userId;
  private String userName;
  private String userEmail;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public CommentResponse(
      Long id,
      Long userId,
      String userName,
      String userEmail,
      String content,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.userId = userId;
    this.userName = userName;
    this.userEmail = userEmail;
    this.content = content;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static CommentResponse from(IssueComment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getUser().getId(),
        comment.getUser().getName(),
        comment.getUser().getEmail(),
        comment.getContent(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public String getContent() {
    return content;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
