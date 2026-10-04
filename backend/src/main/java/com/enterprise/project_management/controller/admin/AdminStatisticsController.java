package com.enterprise.project_management.controller.admin;

import com.enterprise.project_management.entity.*;
import com.enterprise.project_management.repository.*;
import com.enterprise.project_management.service.ActivityService;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {
  private final UserRepository users;
  private final ProjectRepository projects;
  private final IssueRepository issues;
  private final ActivityService activities;

  public AdminStatisticsController(
      UserRepository users,
      ProjectRepository projects,
      IssueRepository issues,
      ActivityService activities) {
    this.users = users;
    this.projects = projects;
    this.issues = issues;
    this.activities = activities;
  }

  @GetMapping
  public Map<String, Object> get(Authentication auth) {
    List<User> allUsers = users.findAll();
    List<Project> allProjects = projects.findAll();
    List<Issue> allIssues = issues.findAll();
    Map<String, Long> userCounts = new LinkedHashMap<>();
    for (Role r : Role.values())
      userCounts.put(
          r.name().toLowerCase(), allUsers.stream().filter(u -> u.getRole() == r).count());
    Map<String, Long> projectCounts = new LinkedHashMap<>();
    for (ProjectStatus s : ProjectStatus.values())
      projectCounts.put(
          s.name().toLowerCase(), allProjects.stream().filter(p -> p.getStatus() == s).count());
    Map<String, Long> issueCounts = new LinkedHashMap<>();
    for (IssueStatus s : IssueStatus.values())
      issueCounts.put(
          s.name().toLowerCase(), allIssues.stream().filter(i -> i.getStatus() == s).count());
    long overdue =
        allIssues.stream()
            .filter(
                i ->
                    i.getDueDate() != null
                        && i.getDueDate().isBefore(LocalDate.now())
                        && i.getStatus() != IssueStatus.RESOLVED
                        && i.getStatus() != IssueStatus.CLOSED)
            .count();
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("totalUsers", allUsers.size());
    response.put("activeUsers", allUsers.stream().filter(User::isActive).count());
    response.put("inactiveUsers", allUsers.stream().filter(u -> !u.isActive()).count());
    response.put("usersByRole", userCounts);
    response.put("totalProjects", allProjects.size());
    response.put("projectsByStatus", projectCounts);
    response.put("totalIssues", allIssues.size());
    response.put("issuesByStatus", issueCounts);
    response.put("overdueIssues", overdue);
    response.put(
        "issuesByPriority",
        Map.of(
            "low",
            allIssues.stream().filter(i -> i.getPriority() == IssuePriority.LOW).count(),
            "medium",
            allIssues.stream().filter(i -> i.getPriority() == IssuePriority.MEDIUM).count(),
            "high",
            allIssues.stream().filter(i -> i.getPriority() == IssuePriority.HIGH).count(),
            "critical",
            allIssues.stream().filter(i -> i.getPriority() == IssuePriority.CRITICAL).count()));
    response.put("recentActivity", activities.list(auth.getName(), null, null));
    response.put(
        "projects",
        allProjects.stream()
            .map(p -> Map.of("id", p.getId(), "name", p.getName(), "status", p.getStatus().name()))
            .toList());
    return response;
  }
}
