package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.project.ProjectMemberResponse;
import com.enterprise.project_management.entity.Project;
import com.enterprise.project_management.entity.ProjectMember;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.ProjectRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectMemberService {

  private static final long MAX_PROJECT_EMPLOYEES = 4;

  private final ProjectMemberRepository projectMemberRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;

  public ProjectMemberService(
      ProjectMemberRepository projectMemberRepository,
      ProjectRepository projectRepository,
      UserRepository userRepository) {
    this.projectMemberRepository = projectMemberRepository;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<ProjectMemberResponse> getMembers(Long projectId, Authentication authentication) {

    User currentUser = getCurrentUser(authentication);

    Project project = getProject(projectId);

    checkViewAccess(project, currentUser);

    return projectMemberRepository.findByProjectId(projectId).stream()
        .map(ProjectMemberResponse::from)
        .toList();
  }

  public ProjectMemberResponse addMember(
      Long projectId, Long userId, Authentication authentication) {

    User currentUser = getCurrentUser(authentication);

    Project project = getProject(projectId);

    checkManageAccess(project, currentUser);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getRole() != Role.EMPLOYEE) {
      throw new IllegalArgumentException("Only employees can be added to a project");
    }

    if (!user.isActive()) {
      throw new IllegalArgumentException("Inactive users cannot be added to a project");
    }

    if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {

      throw new IllegalArgumentException("User is already a member of this project");
    }

    if (projectMemberRepository.countByProjectId(projectId) >= MAX_PROJECT_EMPLOYEES) {
      throw new IllegalArgumentException("A project can have a maximum of 4 employee team members");
    }

    ProjectMember member = new ProjectMember();

    member.setProject(project);
    member.setUser(user);

    ProjectMember savedMember = projectMemberRepository.save(member);

    return ProjectMemberResponse.from(savedMember);
  }

  public void removeMember(Long projectId, Long userId, Authentication authentication) {

    User currentUser = getCurrentUser(authentication);

    Project project = getProject(projectId);

    checkManageAccess(project, currentUser);

    if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {

      throw new IllegalArgumentException("User is not a member of this project");
    }

    projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
  }

  private User getCurrentUser(Authentication authentication) {

    return userRepository
        .findByEmail(authentication.getName())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private Project getProject(Long projectId) {

    return projectRepository
        .findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found"));
  }

  private void checkManageAccess(Project project, User user) {

    if (user.getRole() == Role.ADMIN) {
      return;
    }

    if (user.getRole() == Role.MANAGER
        && project.getManager() != null
        && project.getManager().getId().equals(user.getId())) {

      return;
    }

    throw new IllegalArgumentException("You do not have permission to manage this project team");
  }

  private void checkViewAccess(Project project, User user) {
    if (user.getRole() == Role.ADMIN
        || (project.getManager() != null && project.getManager().getId().equals(user.getId()))
        || projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
      return;
    }
    throw new IllegalArgumentException("Only this project's manager and team members can view its team");
  }
}
