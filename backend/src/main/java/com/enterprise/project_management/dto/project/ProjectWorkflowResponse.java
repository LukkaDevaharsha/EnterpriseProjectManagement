package com.enterprise.project_management.dto.project;

import com.enterprise.project_management.entity.ProjectStatus;
import java.util.List;

public record ProjectWorkflowResponse(
    Long projectId,
    String projectName,
    String description,
    ProjectStatus status,
    int progressPercent,
    List<ProjectWorkflowTaskResponse> tasks) {}
