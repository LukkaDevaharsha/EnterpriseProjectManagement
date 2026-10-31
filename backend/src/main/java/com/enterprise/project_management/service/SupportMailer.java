package com.enterprise.project_management.service;

import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.UserRepository;
import com.enterprise.project_management.repository.ProjectRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.Project;
import org.springframework.stereotype.Service;

@Service
public class SupportMailer {
  private final BrevoEmailClient emailClient;
  private final UserRepository users;
  private final ProjectRepository projects;
  private final ProjectMemberRepository members;
  private final String adminEmail;
  public SupportMailer(BrevoEmailClient emailClient, UserRepository users, ProjectRepository projects, ProjectMemberRepository members, @org.springframework.beans.factory.annotation.Value("${admin.email:}") String adminEmail) {
    this.emailClient = emailClient; this.users = users; this.projects = projects; this.members = members; this.adminEmail = adminEmail;
  }
  public void send(String subject, String details, String email) {
    User user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (user.getRole() != com.enterprise.project_management.entity.Role.MANAGER) throw new org.springframework.security.access.AccessDeniedException("Only managers can contact admin support");
    if (subject == null || subject.isBlank() || subject.length() > 150 || details == null || details.isBlank() || details.length() > 5000) throw new IllegalArgumentException("Enter a subject and issue details (maximum 5000 characters)");
    if (adminEmail.isBlank()) throw new IllegalStateException("Support email is not configured on the server");
    String text = "Support request from " + user.getName() + " (" + user.getEmail() + ")\n\n" + details.trim();
    emailClient.send(adminEmail, "Project support: " + subject.trim(), text, user.getEmail());
  }

  public void sendToManager(Long projectId, String subject, String details, String email) {
    User user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (user.getRole() != Role.EMPLOYEE) throw new org.springframework.security.access.AccessDeniedException("Only employees can contact a project manager");
    if (subject == null || subject.isBlank() || subject.length() > 150 || details == null || details.isBlank() || details.length() > 5000) throw new IllegalArgumentException("Enter a subject and message (maximum 5000 characters)");
    Project project = projects.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
    if (!members.existsByProjectIdAndUserId(projectId, user.getId())) throw new org.springframework.security.access.AccessDeniedException("You can contact the manager only for your assigned projects");
    if (project.getManager() == null) throw new IllegalArgumentException("This project does not have a manager yet");
    String text = "Message from " + user.getName() + " (" + user.getEmail() + ") about project " + project.getName() + ":\n\n" + details.trim();
    emailClient.send(project.getManager().getEmail(), "Project message: " + project.getName() + " — " + subject.trim(), text, user.getEmail());
  }
}
