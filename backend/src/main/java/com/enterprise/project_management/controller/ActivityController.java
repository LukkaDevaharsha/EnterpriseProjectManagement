package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.activity.ActivityResponse;
import com.enterprise.project_management.service.ActivityService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
  private final ActivityService service;

  public ActivityController(ActivityService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<ActivityResponse> list(
      Authentication authentication,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false) Long issueId) {
    return service.list(authentication.getName(), projectId, issueId);
  }
}
