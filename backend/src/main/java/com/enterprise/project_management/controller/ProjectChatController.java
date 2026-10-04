package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.project.ProjectMessageResponse;
import com.enterprise.project_management.service.ProjectChatService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
public class ProjectChatController {
  private final ProjectChatService chat;
  public ProjectChatController(ProjectChatService chat) { this.chat = chat; }
  @GetMapping public List<ProjectMessageResponse> list(@PathVariable Long projectId, Authentication auth) { return chat.list(projectId, auth.getName()); }
  @GetMapping("/unread") public Map<String, Long> unread(@PathVariable Long projectId, Authentication auth) { return Map.of("count", chat.unreadCount(projectId, auth.getName())); }
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public ProjectMessageResponse send(@PathVariable Long projectId, @RequestBody Map<String, String> request, Authentication auth) { return chat.send(projectId, request.get("content"), auth.getName()); }
}
