package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.issue.CreateIssueRequest;
import com.enterprise.project_management.dto.issue.IssueResponse;
import com.enterprise.project_management.dto.issue.UpdateIssueRequest;
import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.IssuePriority;
import com.enterprise.project_management.entity.IssueStatus;
import com.enterprise.project_management.entity.Project;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.ProjectRepository;
import com.enterprise.project_management.repository.UserRepository;
import com.enterprise.project_management.specification.IssueSpecification;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueService {

  private final IssueRepository issueRepository;
  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final UserRepository userRepository;
  private final ActivityService activityService;

  public IssueService(
      IssueRepository issueRepository,
      ProjectRepository projectRepository,
      ProjectMemberRepository projectMemberRepository,
      UserRepository userRepository,
      ActivityService activityService) {
    this.issueRepository = issueRepository;
    this.projectRepository = projectRepository;
    this.projectMemberRepository = projectMemberRepository;
    this.userRepository = userRepository;
    this.activityService = activityService;
  }

  @Transactional
  public IssueResponse createIssue(CreateIssueRequest request, String email) {

    User creator =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Project project =
        projectRepository
            .findById(request.getProjectId())
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

    checkCreateAccess(creator, project);

    Issue issue = new Issue();

    issue.setProject(project);
    issue.setTitle(request.getTitle());
    issue.setDescription(request.getDescription());

    issue.setPriority(request.getPriority() != null ? request.getPriority() : IssuePriority.MEDIUM);

    issue.setDueDate(request.getDueDate());
    issue.setCreatedBy(creator);

    setAssignedUser(issue, project, request.getAssignedTo());

    Issue saved = issueRepository.save(issue);
    activityService.record(
        email, project, saved, "ISSUE_CREATED", "Issue '" + saved.getTitle() + "' was created");
    syncProjectStatus(project);
    return IssueResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public List<IssueResponse> getIssues(
      String email,
      String search,
      IssueStatus status,
      IssuePriority priority,
      Long projectId,
      Long assignedTo) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Specification<Issue> specification =
        Specification.allOf(
            IssueSpecification.search(search),
            IssueSpecification.status(status),
            IssueSpecification.priority(priority),
            IssueSpecification.projectId(projectId));

    if (user.getRole() == Role.ADMIN) {

      if (assignedTo != null) {
        specification = specification.and(IssueSpecification.assignedTo(assignedTo));
      }

    } else if (user.getRole() == Role.MANAGER) {

      specification = specification.and(IssueSpecification.managerId(user.getId()));

      if (assignedTo != null) {
        specification = specification.and(IssueSpecification.assignedTo(assignedTo));
      }

    } else {

      specification = specification.and(IssueSpecification.assignedTo(user.getId()));
    }

    return issueRepository.findAll(specification).stream().map(IssueResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public IssueResponse getIssue(Long issueId, String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    Issue issue =
        issueRepository
            .findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
    if (user.getRole() == Role.ADMIN
        || user.getRole() == Role.MANAGER
            && issue.getProject().getManager() != null
            && issue.getProject().getManager().getId().equals(user.getId())
        || user.getRole() == Role.EMPLOYEE
            && issue.getAssignedTo() != null
            && issue.getAssignedTo().getId().equals(user.getId())) return IssueResponse.from(issue);
    throw new AccessDeniedException("You do not have access to this issue");
  }

  @Transactional
  public IssueResponse updateIssue(Long issueId, UpdateIssueRequest request, String email) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Issue issue =
        issueRepository
            .findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

    checkIssueManageAccess(user, issue);

    issue.setTitle(request.getTitle());
    issue.setDescription(request.getDescription());

    if (request.getPriority() != null) {
      issue.setPriority(request.getPriority());
    }

    issue.setDueDate(request.getDueDate());

    setAssignedUser(issue, issue.getProject(), request.getAssignedTo());

    return IssueResponse.from(issueRepository.save(issue));
  }

  @Transactional
  public IssueResponse updateStatus(Long issueId, IssueStatus newStatus, String email) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Issue issue =
        issueRepository
            .findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

    checkIssueStatusAccess(user, issue);

    if (!isValidTransition(issue.getStatus(), newStatus, user.getRole())) {
      throw new IllegalArgumentException(
          "Invalid status transition from " + issue.getStatus() + " to " + newStatus);
    }

    IssueStatus previous = issue.getStatus();
    boolean previousAssigneeUnavailable = false;
    if (user.getRole() == Role.ADMIN
        && newStatus == IssueStatus.OPEN
        && (previous == IssueStatus.CLOSED || previous == IssueStatus.RESOLVED)
        && issue.getAssignedTo() != null) {
      User assignee = issue.getAssignedTo();
      previousAssigneeUnavailable = !assignee.isActive()
          || assignee.getRole() != Role.EMPLOYEE
          || !projectMemberRepository.existsByProjectIdAndUserId(issue.getProject().getId(), assignee.getId());
      if (previousAssigneeUnavailable) issue.setAssignedTo(null);
    }
    issue.setStatus(newStatus);
    boolean reopened = user.getRole() == Role.ADMIN
        && newStatus == IssueStatus.OPEN
        && (previous == IssueStatus.CLOSED || previous == IssueStatus.RESOLVED);
    String action = reopened ? "ISSUE_REOPENED" : "ISSUE_STATUS_CHANGED";
    String description = reopened
        ? user.getName() + " reopened issue '" + issue.getTitle() + "' from " + previous + " to OPEN."
        : "Issue '" + issue.getTitle() + "' changed from " + previous + " to " + newStatus;
    activityService.recordStatusChange(email, issue.getProject(), issue, action, description, previous, newStatus);

    IssueResponse response = IssueResponse.from(issueRepository.save(issue));
    syncProjectStatus(issue.getProject());
    response.setPreviousAssigneeUnavailable(previousAssigneeUnavailable);
    return response;
  }

  private void syncProjectStatus(Project project) {
    if (project.getStatus() == com.enterprise.project_management.entity.ProjectStatus.ON_HOLD) return;
    List<Issue> projectIssues = issueRepository.findByProjectIdOrderByCreatedAtAscIdAsc(project.getId());
    if (projectIssues.isEmpty()) return;
    boolean allComplete = projectIssues.stream().allMatch(issue ->
        issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED);
    boolean workStarted = projectIssues.stream().anyMatch(issue -> issue.getStatus() != IssueStatus.OPEN);
    if (allComplete) project.setStatus(com.enterprise.project_management.entity.ProjectStatus.COMPLETED);
    else if (workStarted || project.getStatus() == com.enterprise.project_management.entity.ProjectStatus.COMPLETED)
      project.setStatus(com.enterprise.project_management.entity.ProjectStatus.ACTIVE);
    projectRepository.save(project);
  }

  private void setAssignedUser(Issue issue, Project project, Long userId) {

    if (userId == null) {
      issue.setAssignedTo(null);
      return;
    }

    User assignedUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));

    if (assignedUser.getRole() != Role.EMPLOYEE) {
      throw new IllegalArgumentException("Issues can only be assigned to employees");
    }
    if (!assignedUser.isActive())
      throw new IllegalArgumentException("Inactive employees cannot be assigned issues");

    if (!projectMemberRepository.existsByProjectIdAndUserId(
        project.getId(), assignedUser.getId())) {
      throw new IllegalArgumentException("Assigned user is not a member of this project");
    }

    issue.setAssignedTo(assignedUser);
  }

  private void checkCreateAccess(User user, Project project) {

    if (user.getRole() == Role.ADMIN) {
      return;
    }

    if (user.getRole() == Role.MANAGER
        && project.getManager() != null
        && project.getManager().getId().equals(user.getId())) {

      return;
    }

    throw new AccessDeniedException("You do not have permission to create issues for this project");
  }

  private void checkIssueManageAccess(User user, Issue issue) {

    if (user.getRole() == Role.ADMIN) {
      return;
    }

    if (user.getRole() == Role.MANAGER
        && issue.getProject().getManager() != null
        && issue.getProject().getManager().getId().equals(user.getId())) {

      return;
    }

    throw new AccessDeniedException("You do not have permission to edit this issue");
  }

  private void checkIssueStatusAccess(User user, Issue issue) {

    if (user.getRole() == Role.ADMIN) {
      return;
    }

    if (user.getRole() == Role.MANAGER
        && issue.getProject().getManager() != null
        && issue.getProject().getManager().getId().equals(user.getId())) {

      return;
    }

    if (user.getRole() == Role.EMPLOYEE
        && issue.getAssignedTo() != null
        && issue.getAssignedTo().getId().equals(user.getId())) {

      return;
    }

    throw new AccessDeniedException("You do not have permission to update this issue");
  }

  private boolean isValidTransition(IssueStatus current, IssueStatus next, Role role) {

    if (role == Role.ADMIN) return true;

    if (current == next) {
      return true;
    }

    if (role == Role.MANAGER && current == IssueStatus.RESOLVED && next == IssueStatus.OPEN) return true;

    if (role == Role.MANAGER
        && (current == IssueStatus.IN_REVIEW && next == IssueStatus.IN_PROGRESS
            || current == IssueStatus.TESTING && next == IssueStatus.IN_PROGRESS)) return true;

    return switch (current) {
      case OPEN -> next == IssueStatus.IN_PROGRESS;

      case IN_PROGRESS -> next == IssueStatus.IN_REVIEW;

      case IN_REVIEW -> next == IssueStatus.TESTING;

      case TESTING -> next == IssueStatus.RESOLVED;

      case RESOLVED -> next == IssueStatus.CLOSED;

      case CLOSED -> false;
    };
  }
}
