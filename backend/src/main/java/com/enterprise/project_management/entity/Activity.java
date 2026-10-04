package com.enterprise.project_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private User actor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_id")
  private Issue issue;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(name = "old_status", length = 30)
  private String oldStatus;

  @Column(name = "new_status", length = 30)
  private String newStatus;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected Activity() {}

  public Activity(User actor, Project project, Issue issue, String action, String description) {
    this(actor, project, issue, action, description, null, null);
  }

  public Activity(User actor, Project project, Issue issue, String action, String description, String oldStatus, String newStatus) {
    this.actor = actor;
    this.project = project;
    this.issue = issue;
    this.action = action;
    this.description = description;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public User getActor() {
    return actor;
  }

  public Project getProject() {
    return project;
  }

  public Issue getIssue() {
    return issue;
  }

  public String getAction() {
    return action;
  }

  public String getDescription() {
    return description;
  }

  public String getOldStatus() { return oldStatus; }

  public String getNewStatus() { return newStatus; }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
