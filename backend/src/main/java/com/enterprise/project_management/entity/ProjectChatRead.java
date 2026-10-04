package com.enterprise.project_management.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "project_chat_reads", uniqueConstraints = @UniqueConstraint(
    name = "uk_project_chat_reads_project_user", columnNames = {"project_id", "user_id"}))
public class ProjectChatRead {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false) private Project project;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
  @Column(name = "last_read_message_id", nullable = false) private Long lastReadMessageId = 0L;
  public Long getId() { return id; }
  public Project getProject() { return project; }
  public void setProject(Project project) { this.project = project; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public Long getLastReadMessageId() { return lastReadMessageId; }
  public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }
}
