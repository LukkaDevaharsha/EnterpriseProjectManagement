package com.enterprise.project_management.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "issues")
public class Issue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "project_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_issues_project"))
  private Project project;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private IssuePriority priority = IssuePriority.MEDIUM;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private IssueStatus status = IssueStatus.OPEN;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_to", foreignKey = @ForeignKey(name = "fk_issues_assigned_to"))
  private User assignedTo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "created_by",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_issues_created_by"))
  private User createdBy;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public Issue() {}

  public Long getId() {
    return id;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public IssuePriority getPriority() {
    return priority;
  }

  public void setPriority(IssuePriority priority) {
    this.priority = priority;
  }

  public IssueStatus getStatus() {
    return status;
  }

  public void setStatus(IssueStatus status) {
    this.status = status;
  }

  public User getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(User assignedTo) {
    this.assignedTo = assignedTo;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
