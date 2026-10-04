package com.enterprise.project_management.service;

import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.UserRepository;
import com.enterprise.project_management.repository.ProjectRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SupportMailer {
  private final JavaMailSender sender;
  private final UserRepository users;
  private final ProjectRepository projects;
  private final ProjectMemberRepository members;
  private final String from;
  private final String adminEmail;
  public SupportMailer(JavaMailSender sender, UserRepository users, ProjectRepository projects, ProjectMemberRepository members, @Value("${app.mail.from:}") String from, @Value("${admin.email:}") String adminEmail) {
    this.sender = sender; this.users = users; this.projects = projects; this.members = members; this.from = from; this.adminEmail = adminEmail;
  }
  public void send(String subject, String details, String email) {
    User user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (user.getRole() != com.enterprise.project_management.entity.Role.MANAGER) throw new org.springframework.security.access.AccessDeniedException("Only managers can contact admin support");
    if (subject == null || subject.isBlank() || subject.length() > 150 || details == null || details.isBlank() || details.length() > 5000) throw new IllegalArgumentException("Enter a subject and issue details (maximum 5000 characters)");
    if (from.isBlank() || adminEmail.isBlank()) throw new IllegalStateException("Support email is not configured on the server");
    SimpleMailMessage message = new SimpleMailMessage(); message.setFrom(from); message.setTo(adminEmail);
    message.setReplyTo(user.getEmail()); message.setSubject("Project support: " + subject.trim());
    message.setText("Support request from " + user.getName() + " (" + user.getEmail() + ")\n\n" + details.trim()); sender.send(message);
  }

  public void sendToManager(Long projectId, String subject, String details, String email) {
    User user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (user.getRole() != Role.EMPLOYEE) throw new org.springframework.security.access.AccessDeniedException("Only employees can contact a project manager");
    if (subject == null || subject.isBlank() || subject.length() > 150 || details == null || details.isBlank() || details.length() > 5000) throw new IllegalArgumentException("Enter a subject and message (maximum 5000 characters)");
    Project project = projects.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
    if (!members.existsByProjectIdAndUserId(projectId, user.getId())) throw new org.springframework.security.access.AccessDeniedException("You can contact the manager only for your assigned projects");
    if (project.getManager() == null) throw new IllegalArgumentException("This project does not have a manager yet");
    if (from.isBlank()) throw new IllegalStateException("Email is not configured on the server");
    SimpleMailMessage message = new SimpleMailMessage(); message.setFrom(from); message.setTo(project.getManager().getEmail());
    message.setReplyTo(user.getEmail()); message.setSubject("Project message: " + project.getName() + " — " + subject.trim());
    message.setText("Message from " + user.getName() + " (" + user.getEmail() + ") about project " + project.getName() + ":\n\n" + details.trim()); sender.send(message);
  }
}
