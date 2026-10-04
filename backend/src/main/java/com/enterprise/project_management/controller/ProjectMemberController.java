package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.project.AddProjectMemberRequest;
import com.enterprise.project_management.dto.project.ProjectMemberResponse;
import com.enterprise.project_management.service.ProjectMemberService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

  private final ProjectMemberService projectMemberService;

  public ProjectMemberController(ProjectMemberService projectMemberService) {
    this.projectMemberService = projectMemberService;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<ProjectMemberResponse> getMembers(
      @PathVariable Long projectId, Authentication authentication) {

    return projectMemberService.getMembers(projectId, authentication);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ProjectMemberResponse addMember(
      @PathVariable Long projectId,
      @RequestBody AddProjectMemberRequest request,
      Authentication authentication) {

    return projectMemberService.addMember(projectId, request.getUserId(), authentication);
  }

  @DeleteMapping("/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public String removeMember(
      @PathVariable Long projectId, @PathVariable Long userId, Authentication authentication) {

    projectMemberService.removeMember(projectId, userId, authentication);

    return "Employee removed from project";
  }
}
