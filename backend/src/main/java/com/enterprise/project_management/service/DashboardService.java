package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.DashboardResponse;
import com.enterprise.project_management.entity.IssueStatus;
import com.enterprise.project_management.entity.Project;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.ProjectRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final IssueRepository issueRepository;

  public DashboardService(
      UserRepository userRepository,
      ProjectRepository projectRepository,
      ProjectMemberRepository projectMemberRepository,
      IssueRepository issueRepository) {
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.projectMemberRepository = projectMemberRepository;
    this.issueRepository = issueRepository;
  }

  public DashboardResponse getDashboard(Authentication authentication) {

    User user =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getRole() == Role.ADMIN) {
      return getAdminDashboard();
    }

    if (user.getRole() == Role.MANAGER) {
      return getManagerDashboard(user.getId());
    }

    return getEmployeeDashboard(user.getId());
  }

  private DashboardResponse getAdminDashboard() {

    long totalUsers = userRepository.count();

    long totalProjects = projectRepository.count();

    long totalIssues = issueRepository.count();

    long openIssues = issueRepository.countByStatus(IssueStatus.OPEN);

    long inProgressIssues = issueRepository.countByStatus(IssueStatus.IN_PROGRESS);

    long resolvedIssues = issueRepository.countByStatus(IssueStatus.RESOLVED);

    return new DashboardResponse(
        Role.ADMIN,
        totalUsers,
        totalProjects,
        totalIssues,
        openIssues,
        inProgressIssues,
        resolvedIssues);
  }

  private DashboardResponse getManagerDashboard(Long managerId) {

    List<Project> projects = projectRepository.findByManagerId(managerId);

    long totalProjects = projects.size();

    long totalIssues = 0;
    long openIssues = 0;
    long inProgressIssues = 0;
    long resolvedIssues = 0;

    for (Project project : projects) {

      Long projectId = project.getId();

      totalIssues += issueRepository.countByProjectId(projectId);

      openIssues += issueRepository.countByProjectIdAndStatus(projectId, IssueStatus.OPEN);

      inProgressIssues +=
          issueRepository.countByProjectIdAndStatus(projectId, IssueStatus.IN_PROGRESS);

      resolvedIssues += issueRepository.countByProjectIdAndStatus(projectId, IssueStatus.RESOLVED);
    }

    return new DashboardResponse(
        Role.MANAGER, 0L, totalProjects, totalIssues, openIssues, inProgressIssues, resolvedIssues);
  }

  private DashboardResponse getEmployeeDashboard(Long employeeId) {

    long totalProjects = projectMemberRepository.countByUserId(employeeId);

    long totalIssues = issueRepository.countByAssignedToId(employeeId);

    long openIssues = issueRepository.countByAssignedToIdAndStatus(employeeId, IssueStatus.OPEN);

    long inProgressIssues =
        issueRepository.countByAssignedToIdAndStatus(employeeId, IssueStatus.IN_PROGRESS);

    long resolvedIssues =
        issueRepository.countByAssignedToIdAndStatus(employeeId, IssueStatus.RESOLVED);

    return new DashboardResponse(
        Role.EMPLOYEE,
        0L,
        totalProjects,
        totalIssues,
        openIssues,
        inProgressIssues,
        resolvedIssues);
  }
}
