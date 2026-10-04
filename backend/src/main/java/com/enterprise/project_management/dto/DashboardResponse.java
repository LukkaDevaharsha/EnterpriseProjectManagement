package com.enterprise.project_management.dto;

import com.enterprise.project_management.entity.Role;

public class DashboardResponse {

  private Role role;
  private long totalUsers;
  private long totalProjects;
  private long totalIssues;
  private long openIssues;
  private long inProgressIssues;
  private long resolvedIssues;

  public DashboardResponse() {}

  public DashboardResponse(
      Role role,
      long totalUsers,
      long totalProjects,
      long totalIssues,
      long openIssues,
      long inProgressIssues,
      long resolvedIssues) {
    this.role = role;
    this.totalUsers = totalUsers;
    this.totalProjects = totalProjects;
    this.totalIssues = totalIssues;
    this.openIssues = openIssues;
    this.inProgressIssues = inProgressIssues;
    this.resolvedIssues = resolvedIssues;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public long getTotalUsers() {
    return totalUsers;
  }

  public void setTotalUsers(long totalUsers) {
    this.totalUsers = totalUsers;
  }

  public long getTotalProjects() {
    return totalProjects;
  }

  public void setTotalProjects(long totalProjects) {
    this.totalProjects = totalProjects;
  }

  public long getTotalIssues() {
    return totalIssues;
  }

  public void setTotalIssues(long totalIssues) {
    this.totalIssues = totalIssues;
  }

  public long getOpenIssues() {
    return openIssues;
  }

  public void setOpenIssues(long openIssues) {
    this.openIssues = openIssues;
  }

  public long getInProgressIssues() {
    return inProgressIssues;
  }

  public void setInProgressIssues(long inProgressIssues) {
    this.inProgressIssues = inProgressIssues;
  }

  public long getResolvedIssues() {
    return resolvedIssues;
  }

  public void setResolvedIssues(long resolvedIssues) {
    this.resolvedIssues = resolvedIssues;
  }
}
