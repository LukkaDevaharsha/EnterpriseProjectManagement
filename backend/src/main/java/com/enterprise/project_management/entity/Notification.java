package com.enterprise.project_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipient_id", nullable = false)
  private User recipient;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private User actor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "issue_id", nullable = false)
  private Issue issue;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 1000)
  private String message;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  protected Notification() {}

  public Notification(User recipient, User actor, Project project, Issue issue, String title, String message) {
    this.recipient = recipient;
    this.actor = actor;
    this.project = project;
    this.issue = issue;
    this.title = title;
    this.message = message;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public User getRecipient() { return recipient; }
  public User getActor() { return actor; }
  public Project getProject() { return project; }
  public Issue getIssue() { return issue; }
  public String getTitle() { return title; }
  public String getMessage() { return message; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getReadAt() { return readAt; }
  public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
