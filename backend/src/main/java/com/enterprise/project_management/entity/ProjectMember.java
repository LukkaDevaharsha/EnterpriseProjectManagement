package com.enterprise.project_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_project_member",
          columnNames = {"project_id", "user_id"})
    })
public class ProjectMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "project_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_project_members_project"))
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_project_members_user"))
  private User user;

  @CreationTimestamp
  @Column(name = "assigned_at", nullable = false)
  private LocalDateTime assignedAt;

  public ProjectMember() {}

  public Long getId() {
    return id;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDateTime getAssignedAt() {
    return assignedAt;
  }
}
