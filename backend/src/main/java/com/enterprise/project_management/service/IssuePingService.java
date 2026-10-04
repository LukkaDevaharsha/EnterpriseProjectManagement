package com.enterprise.project_management.service;

import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.Notification;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.NotificationRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssuePingService {
  private final IssueRepository issues;
  private final UserRepository users;
  private final NotificationRepository notifications;
  private final ActivityService activities;

  public IssuePingService(IssueRepository issues, UserRepository users,
      NotificationRepository notifications, ActivityService activities) {
    this.issues = issues;
    this.users = users;
    this.notifications = notifications;
    this.activities = activities;
  }

  @Transactional
  public Map<String, Object> ping(Long issueId, String email) {
    User actor = users.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    Issue issue = issues.findById(issueId)
        .orElseThrow(() -> new IllegalArgumentException("Issue not found"));
    User manager = issue.getProject().getManager();
    if (actor.getRole() == Role.MANAGER) {
      if (manager == null || !manager.getId().equals(actor.getId()))
        throw new AccessDeniedException("You can only ping issues in projects you manage");
    } else if (actor.getRole() != Role.ADMIN) {
      throw new AccessDeniedException("Only admins and project managers can ping issue assignees");
    }

    Map<Long, User> recipients = new LinkedHashMap<>();
    if (actor.getRole() == Role.ADMIN && manager != null
        && manager.isActive() && manager.getRole() == Role.MANAGER) {
      recipients.put(manager.getId(), manager);
    }
    User assignee = issue.getAssignedTo();
    if (assignee != null && assignee.isActive() && assignee.getRole() == Role.EMPLOYEE) {
      recipients.put(assignee.getId(), assignee);
    }
    if (recipients.isEmpty())
      throw new IllegalArgumentException("No active project manager or assigned employee is available to notify");

    String roleLabel = actor.getRole() == Role.ADMIN ? "Admin" : "Manager";
    String title = "Reminder: " + issue.getTitle();
    if (title.length() > 200) title = title.substring(0, 200);
    String message = roleLabel + " " + actor.getName() + " pinged you about issue '"
        + issue.getTitle() + "' in project '" + issue.getProject().getName() + "'.";
    for (User recipient : recipients.values()) {
      notifications.save(new Notification(recipient, actor, issue.getProject(), issue, title, message));
    }
    String recipientNames = recipients.values().stream().map(User::getName)
        .reduce((first, next) -> first + " and " + next).orElse("the assignee");
    activities.record(email, issue.getProject(), issue, "ISSUE_PINGED",
        roleLabel + " " + actor.getName() + " sent an issue reminder to " + recipientNames + ".");
    return Map.of("message", recipients.size() == 1
        ? "Issue reminder sent to " + recipientNames + "."
        : "Issue reminder sent to " + recipients.size() + " people: " + recipientNames + ".",
        "notifiedCount", recipients.size());
  }

  @Transactional(readOnly = true)
  public com.enterprise.project_management.dto.notification.NotificationInboxResponse inbox(String email) {
    User recipient = users.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    List<com.enterprise.project_management.dto.notification.NotificationResponse> items =
        notifications.findTop50ByRecipientIdOrderByCreatedAtDesc(recipient.getId()).stream()
            .map(com.enterprise.project_management.dto.notification.NotificationResponse::from).toList();
    return new com.enterprise.project_management.dto.notification.NotificationInboxResponse(
        items, notifications.countByRecipientIdAndReadAtIsNull(recipient.getId()));
  }

  @Transactional
  public void markRead(Long notificationId, String email) {
    User recipient = users.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    Notification notification = notifications.findByIdAndRecipientId(notificationId, recipient.getId())
        .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    if (notification.getReadAt() == null) notification.setReadAt(java.time.LocalDateTime.now());
    notifications.save(notification);
  }

  @Transactional
  public void markAllRead(String email) {
    User recipient = users.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    notifications.findByRecipientIdAndReadAtIsNull(recipient.getId()).forEach(item -> item.setReadAt(now));
  }
}
