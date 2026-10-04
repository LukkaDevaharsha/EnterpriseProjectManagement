package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.activity.ActivityResponse;
import com.enterprise.project_management.entity.*;
import com.enterprise.project_management.repository.ActivityRepository;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.ProjectRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {
  private final ActivityRepository repository;
  private final UserRepository users;
  private final ProjectMemberRepository members;
  private final ProjectRepository projects;
  private final IssueRepository issues;

  public ActivityService(
      ActivityRepository repository,
      UserRepository users,
      ProjectMemberRepository members,
      ProjectRepository projects,
      IssueRepository issues) {
    this.repository = repository;
    this.users = users;
    this.members = members;
    this.projects = projects;
    this.issues = issues;
  }

  public void record(
      String email, Project project, Issue issue, String action, String description) {
    users
        .findByEmail(email)
        .ifPresent(
            user -> repository.save(new Activity(user, project, issue, action, description)));
  }

  public void recordStatusChange(
      String email, Project project, Issue issue, String action, String description,
      IssueStatus oldStatus, IssueStatus newStatus) {
    users.findByEmail(email).ifPresent(user -> repository.save(new Activity(
        user, project, issue, action, description, oldStatus.name(), newStatus.name())));
  }

  public List<ActivityResponse> list(String email, Long projectId, Long issueId) {
    User user =
        users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    List<Activity> items;
    if (user.getRole() == Role.ADMIN) {
      items =
          issueId != null
              ? repository.findTop100ByIssueIdOrderByCreatedAtDesc(issueId)
              : projectId != null
                  ? repository.findTop100ByProjectIdOrderByCreatedAtDesc(projectId)
                  : repository.findTop100ByOrderByCreatedAtDesc();
    } else if (issueId != null) {
      Issue issue =
          issues
              .findById(issueId)
              .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
      if (!allowed(user, issue.getProject().getId())
          && (issue.getAssignedTo() == null || !issue.getAssignedTo().getId().equals(user.getId())))
        throw new AccessDeniedException("You do not have access to this issue activity");
      items = repository.findTop100ByIssueIdOrderByCreatedAtDesc(issueId);
    } else if (projectId != null) {
      if (!allowed(user, projectId))
        throw new AccessDeniedException("You do not have access to this project activity");
      items = repository.findTop100ByProjectIdOrderByCreatedAtDesc(projectId);
    } else if (user.getRole() == Role.MANAGER) {
      items =
          repository.findTop100ByOrderByCreatedAtDesc().stream()
              .filter(
                  a ->
                      a.getProject() != null
                          && a.getProject().getManager() != null
                          && a.getProject().getManager().getId().equals(user.getId()))
              .toList();
    } else {
      items =
          repository.findTop100ByOrderByCreatedAtDesc().stream()
              .filter(
                  a ->
                      a.getIssue() != null
                          && a.getIssue().getAssignedTo() != null
                          && a.getIssue().getAssignedTo().getId().equals(user.getId()))
              .toList();
    }
    return items.stream().map(ActivityResponse::from).toList();
  }

  private boolean allowed(User u, Long id) {
    if (u.getRole() == Role.MANAGER)
      return projects
          .findById(id)
          .map(p -> p.getManager() != null && p.getManager().getId().equals(u.getId()))
          .orElse(false);
    return members.existsByProjectIdAndUserId(id, u.getId());
  }
}
