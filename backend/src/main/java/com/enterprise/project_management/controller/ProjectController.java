package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.project.CreateProjectRequest;
import com.enterprise.project_management.dto.project.ProjectResponse;
import com.enterprise.project_management.dto.project.UpdateProjectRequest;
import com.enterprise.project_management.dto.project.ProjectWorkflowResponse;
import com.enterprise.project_management.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<ProjectResponse> getProjects(Authentication authentication) {
    return projectService.getProjectsForUser(authentication);
  }

  @GetMapping("/{projectId}")
  @PreAuthorize("isAuthenticated()")
  public ProjectResponse getProjectById(
      @PathVariable Long projectId, Authentication authentication) {
    return projectService.getProjectById(projectId, authentication);
  }

  @GetMapping("/{projectId}/workflow")
  @PreAuthorize("isAuthenticated()")
  public ProjectWorkflowResponse getWorkflow(
      @PathVariable Long projectId, Authentication authentication) {
    return projectService.getWorkflow(projectId, authentication);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ProjectResponse createProject(
      @Valid @RequestBody CreateProjectRequest request, Authentication authentication) {
    return projectService.createProject(request, authentication);
  }

  @PutMapping("/{projectId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ProjectResponse updateProject(
      @PathVariable Long projectId,
      @Valid @RequestBody UpdateProjectRequest request,
      Authentication authentication) {
    return projectService.updateProject(projectId, request, authentication);
  }

  @PatchMapping("/{projectId}/status")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ProjectResponse updateStatus(
      @PathVariable Long projectId,
      @RequestBody UpdateProjectRequest request,
      Authentication authentication) {
    return projectService.updateProject(projectId, request, authentication);
  }

  @DeleteMapping("/{projectId}")
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteProject(@PathVariable Long projectId, Authentication authentication) {
    projectService.deleteProject(projectId, authentication);
  }
}
