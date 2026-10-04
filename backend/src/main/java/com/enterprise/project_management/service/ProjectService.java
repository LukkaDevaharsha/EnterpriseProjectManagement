package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.project.CreateProjectRequest;
import com.enterprise.project_management.dto.project.ProjectResponse;
import com.enterprise.project_management.dto.project.ProjectWorkflowResponse;
import com.enterprise.project_management.dto.project.ProjectWorkflowTaskResponse;
import com.enterprise.project_management.dto.project.UpdateProjectRequest;
import com.enterprise.project_management.entity.IssueStatus;
import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.IssuePriority;
import com.enterprise.project_management.entity.Project;
import com.enterprise.project_management.entity.ProjectMember;
import com.enterprise.project_management.entity.ProjectStatus;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.ProjectRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final IssueRepository issueRepository;
  private final ActivityService activityService;

  public ProjectService(
      ProjectRepository projectRepository,
      UserRepository userRepository,
      ProjectMemberRepository projectMemberRepository,
      IssueRepository issueRepository,
      ActivityService activityService) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.projectMemberRepository = projectMemberRepository;
    this.issueRepository = issueRepository;
    this.activityService = activityService;
  }

  @Transactional
  public ProjectResponse createProject(
      CreateProjectRequest request, Authentication authentication) {

    if (projectRepository.existsByName(request.getName())) {
      throw new IllegalArgumentException("Project name already exists");
    }

    User currentUser =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Project project = new Project();

    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setStatus(ProjectStatus.PLANNING);
    project.setStartDate(request.getStartDate());
    project.setEndDate(request.getEndDate());

    /*
     * Manager:
     * The logged-in manager automatically becomes
     * the manager of the new project.
     */
    if (currentUser.getRole() == Role.MANAGER) {

      project.setManager(currentUser);
    }

    /*
     * Admin:
     * Admin can choose a manager for the project.
     */
    if (currentUser.getRole() == Role.ADMIN && request.getManagerId() != null) {

      User manager =
          userRepository
              .findById(request.getManagerId())
              .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

      if (manager.getRole() != Role.MANAGER) {
        throw new IllegalArgumentException("Selected user is not a manager");
      }
      if (!manager.isActive())
        throw new IllegalArgumentException("Inactive users cannot manage projects");

      project.setManager(manager);
    }

    Project savedProject = projectRepository.save(project);
    activityService.record(
        authentication.getName(),
        savedProject,
        null,
        "PROJECT_CREATED",
        "Project '" + savedProject.getName() + "' was created");

    if (request.getTasks() != null) {
      for (var taskRequest : request.getTasks()) {
        Issue task = new Issue();
        task.setProject(savedProject);
        task.setTitle(taskRequest.getTitle().trim());
        task.setDescription(taskRequest.getDescription());
        task.setPriority(taskRequest.getPriority() == null ? IssuePriority.MEDIUM : taskRequest.getPriority());
        task.setDueDate(taskRequest.getDueDate());
        task.setCreatedBy(currentUser);
        Issue savedTask = issueRepository.save(task);
        activityService.record(authentication.getName(), savedProject, savedTask,
            "ISSUE_CREATED", "Project step '" + savedTask.getTitle() + "' was created");
      }
    }

    return response(savedProject);
  }

  @Transactional(readOnly = true)
  public ProjectWorkflowResponse getWorkflow(Long projectId, Authentication authentication) {
    User user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    Project project = getProjectEntity(projectId);
    checkProjectAccess(project, user);
    List<Issue> tasks = issueRepository.findByProjectIdOrderByCreatedAtAscIdAsc(projectId);
    int progress = tasks.isEmpty() ? 0 : (int) Math.round(
        tasks.stream().filter(task -> task.getStatus() == IssueStatus.RESOLVED || task.getStatus() == IssueStatus.CLOSED).count()
            * 100.0 / tasks.size());
    List<ProjectWorkflowTaskResponse> taskResponses = new ArrayList<>();
    for (int index = 0; index < tasks.size(); index++) {
      taskResponses.add(ProjectWorkflowTaskResponse.from(tasks.get(index), index + 1));
    }
    return new ProjectWorkflowResponse(project.getId(), project.getName(), project.getDescription(),
        project.getStatus(), progress, taskResponses);
  }

  public List<ProjectResponse> getProjectsForUser(Authentication authentication) {

    User user =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    return getProjectsForUser(user);
  }

  public List<ProjectResponse> getProjectsForUser(User user) {

    List<Project> projects = new ArrayList<>();

    if (user.getRole() == Role.ADMIN) {

      projects.addAll(projectRepository.findAll());

    } else if (user.getRole() == Role.MANAGER) {

      projects.addAll(projectRepository.findByManagerId(user.getId()));

    } else {

      List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId());

      for (ProjectMember membership : memberships) {

        Project project = membership.getProject();

        if (project != null) {
          projects.add(project);
        }
      }
    }

    return projects.stream().map(this::response).toList();
  }

  public ProjectResponse getProjectById(Long projectId, Authentication authentication) {

    User user =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

    checkProjectAccess(project, user);

    return response(project);
  }

  @Transactional
  public ProjectResponse updateProject(
      Long projectId, UpdateProjectRequest request, Authentication authentication) {
    User actor =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    Project project = getProjectEntity(projectId);
    checkProjectManageAccess(project, actor);
    if (request.getName() != null
        && !request.getName().equals(project.getName())
        && projectRepository.existsByName(request.getName()))
      throw new IllegalArgumentException("Project name already exists");
    if (request.getName() != null) project.setName(request.getName());
    if (request.getDescription() != null) project.setDescription(request.getDescription());
    if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
    if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
    if (project.getStartDate() != null
        && project.getEndDate() != null
        && project.getEndDate().isBefore(project.getStartDate()))
      throw new IllegalArgumentException("End date cannot be before start date");
    if (request.getStatus() != null) project.setStatus(request.getStatus());
    if (request.getManagerId() != null) {
      if (actor.getRole() != Role.ADMIN)
        throw new org.springframework.security.access.AccessDeniedException(
            "Only admins can change the project manager");
      User manager =
          userRepository
              .findById(request.getManagerId())
              .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
      if (manager.getRole() != Role.MANAGER || !manager.isActive())
        throw new IllegalArgumentException("Selected manager must be an active manager");
      project.setManager(manager);
    }
    Project saved = projectRepository.save(project);
    activityService.record(
        authentication.getName(),
        saved,
        null,
        "PROJECT_UPDATED",
        "Project '" + saved.getName() + "' was updated");
    return response(saved);
  }

  @Transactional
  public void deleteProject(Long projectId, Authentication authentication) {
    User actor =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (actor.getRole() != Role.ADMIN)
      throw new org.springframework.security.access.AccessDeniedException(
          "Only admins can delete projects");
    Project project = getProjectEntity(projectId);
    if (issueRepository.countByProjectId(projectId) > 0)
      throw new IllegalArgumentException(
          "Project has issues and cannot be deleted. Resolve or remove its issues first.");
    projectMemberRepository.deleteAll(projectMemberRepository.findByProjectId(projectId));
    String projectName = project.getName();
    projectRepository.delete(project);
    activityService.record(
        authentication.getName(),
        null,
        null,
        "PROJECT_DELETED",
        "Project '" + projectName + "' was deleted");
  }

  private ProjectResponse response(Project project) {
    long total = issueRepository.countByProjectId(project.getId());
    if (total == 0) return ProjectResponse.from(project, 0);
    long complete =
        issueRepository.countByProjectIdAndStatusIn(
            project.getId(), List.of(IssueStatus.RESOLVED, IssueStatus.CLOSED));
    return ProjectResponse.from(project, (int) Math.round(complete * 100.0 / total));
  }

  private void checkProjectManageAccess(Project project, User user) {
    if (user.getRole() == Role.ADMIN) return;
    if (user.getRole() == Role.MANAGER
        && project.getManager() != null
        && project.getManager().getId().equals(user.getId())) return;
    throw new org.springframework.security.access.AccessDeniedException(
        "You do not have permission to manage this project");
  }

  public Project getProjectEntity(Long projectId) {

    return projectRepository
        .findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found"));
  }

  private void checkProjectAccess(Project project, User user) {

    if (user.getRole() == Role.ADMIN) {
      return;
    }

    if (user.getRole() == Role.MANAGER) {

      if (project.getManager() == null || !project.getManager().getId().equals(user.getId())) {

        throw new IllegalArgumentException("You do not have access to this project");
      }

      return;
    }

    boolean member =
        projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId());

    if (!member) {
      throw new IllegalArgumentException("You do not have access to this project");
    }
  }
}
