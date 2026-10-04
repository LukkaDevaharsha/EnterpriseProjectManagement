package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.issue.CreateIssueRequest;
import com.enterprise.project_management.dto.issue.IssueResponse;
import com.enterprise.project_management.dto.issue.UpdateIssueRequest;
import com.enterprise.project_management.dto.issue.UpdateIssueStatusRequest;
import com.enterprise.project_management.service.IssueService;
import com.enterprise.project_management.service.IssuePingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

  private final IssueService issueService;
  private final IssuePingService pingService;

  public IssueController(IssueService issueService, IssuePingService pingService) {
    this.issueService = issueService;
    this.pingService = pingService;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<?> createIssue(
      @Valid @RequestBody CreateIssueRequest request, Authentication authentication) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(issueService.createIssue(request, authentication.getName()));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> getIssues(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) com.enterprise.project_management.entity.IssueStatus status,
      @RequestParam(required = false)
          com.enterprise.project_management.entity.IssuePriority priority,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false) Long assignedTo,
      Authentication authentication) {

    try {

      List<IssueResponse> issues =
          issueService.getIssues(
              authentication.getName(), search, status, priority, projectId, assignedTo);

      return ResponseEntity.ok(issues);

    } catch (IllegalArgumentException e) {

      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping("/{issueId}")
  @PreAuthorize("isAuthenticated()")
  public IssueResponse getIssue(@PathVariable Long issueId, Authentication authentication) {
    return issueService.getIssue(issueId, authentication.getName());
  }

  @PutMapping("/{issueId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<?> updateIssue(
      @PathVariable Long issueId,
      @Valid @RequestBody UpdateIssueRequest request,
      Authentication authentication) {
    try {
      return ResponseEntity.ok(
          issueService.updateIssue(issueId, request, authentication.getName()));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
  }

  @PatchMapping("/{issueId}/status")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> updateStatus(
      @PathVariable Long issueId,
      @Valid @RequestBody UpdateIssueStatusRequest request,
      Authentication authentication) {
    try {
      return ResponseEntity.ok(
          issueService.updateStatus(issueId, request.getStatus(), authentication.getName()));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/{issueId}/ping")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<?> ping(@PathVariable Long issueId, Authentication authentication) {
    try {
      return ResponseEntity.ok(pingService.ping(issueId, authentication.getName()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
  }
}
