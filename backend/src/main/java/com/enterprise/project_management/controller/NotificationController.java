package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.notification.NotificationInboxResponse;
import com.enterprise.project_management.service.IssuePingService;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {
  private final IssuePingService service;

  public NotificationController(IssuePingService service) { this.service = service; }

  @GetMapping
  public NotificationInboxResponse inbox(Authentication authentication) {
    return service.inbox(authentication.getName());
  }

  @PatchMapping("/{notificationId}/read")
  public Map<String, String> markRead(@PathVariable Long notificationId, Authentication authentication) {
    service.markRead(notificationId, authentication.getName());
    return Map.of("message", "Notification marked as read");
  }

  @PatchMapping("/read-all")
  public Map<String, String> markAllRead(Authentication authentication) {
    service.markAllRead(authentication.getName());
    return Map.of("message", "Notifications marked as read");
  }
}
