package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.project.ProjectMessageResponse;
import com.enterprise.project_management.entity.*;
import com.enterprise.project_management.repository.*;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectChatService {
  private final ProjectRepository projects;
  private final ProjectMemberRepository members;
  private final UserRepository users;
  private final ProjectMessageRepository messages;
  private final ProjectChatReadRepository reads;
  public ProjectChatService(ProjectRepository projects, ProjectMemberRepository members, UserRepository users, ProjectMessageRepository messages, ProjectChatReadRepository reads) {
    this.projects = projects; this.members = members; this.users = users; this.messages = messages; this.reads = reads;
  }
  @Transactional
  public List<ProjectMessageResponse> list(Long projectId, String email) {
    User user = user(email); Project project = project(projectId); access(project, user);
    markRead(project, user);
    return messages.findTop100ByProjectIdOrderByCreatedAtAsc(projectId).stream().map(ProjectMessageResponse::from).toList();
  }
  @Transactional(readOnly = true)
  public long unreadCount(Long projectId, String email) {
    User user = user(email); Project project = project(projectId); access(project, user);
    Long lastReadId = reads.findByProjectIdAndUserId(projectId, user.getId())
        .map(ProjectChatRead::getLastReadMessageId).orElse(0L);
    return messages.countByProjectIdAndSenderIdNotAndIdGreaterThan(projectId, user.getId(), lastReadId);
  }
  @Transactional
  public ProjectMessageResponse send(Long projectId, String content, String email) {
    if (content == null || content.isBlank() || content.length() > 2000) throw new IllegalArgumentException("Message must contain 1 to 2000 characters");
    User user = user(email); Project project = project(projectId); access(project, user);
    ProjectMessage message = new ProjectMessage(); message.setProject(project); message.setSender(user); message.setContent(content.trim());
    return ProjectMessageResponse.from(messages.save(message));
  }
  private User user(String email) { return users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found")); }
  private Project project(Long id) { return projects.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found")); }
  private void markRead(Project project, User user) {
    ProjectChatRead read = reads.findByProjectIdAndUserId(project.getId(), user.getId()).orElseGet(() -> {
      ProjectChatRead created = new ProjectChatRead(); created.setProject(project); created.setUser(user); return created;
    });
    read.setLastReadMessageId(messages.findLatestIdByProjectId(project.getId())); reads.save(read);
  }
  private void access(Project project, User user) {
    if (user.getRole() == Role.ADMIN || (project.getManager() != null && project.getManager().getId().equals(user.getId())) || members.existsByProjectIdAndUserId(project.getId(), user.getId())) return;
    throw new AccessDeniedException("Only this project's manager and team members can access its chat");
  }
}
