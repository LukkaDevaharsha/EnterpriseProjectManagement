package com.enterprise.project_management.dto.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

  @NotBlank(message = "Comment is required")
  @Size(max = 2000, message = "Comment must not exceed 2000 characters")
  private String content;

  public CreateCommentRequest() {}

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
