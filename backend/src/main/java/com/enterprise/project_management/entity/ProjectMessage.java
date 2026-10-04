package com.enterprise.project_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "project_messages")
public class ProjectMessage {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false) private Project project;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sender_id", nullable = false) private User sender;
  @Column(nullable = false, length = 2000) private String content;
  @CreationTimestamp @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
  public Long getId() { return id; }
  public Project getProject() { return project; }
  public void setProject(Project project) { this.project = project; }
  public User getSender() { return sender; }
  public void setSender(User sender) { this.sender = sender; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
