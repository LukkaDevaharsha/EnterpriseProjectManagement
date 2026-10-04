package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.issue.CommentResponse;
import com.enterprise.project_management.dto.issue.CreateCommentRequest;
import com.enterprise.project_management.service.IssueCommentService;
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
@RequestMapping("/api/issues/{issueId}/comments")
@PreAuthorize("isAuthenticated()")
public class IssueCommentController {

  private final IssueCommentService commentService;

  public IssueCommentController(IssueCommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping
  public ResponseEntity<?> addComment(
      @PathVariable Long issueId,
      @Valid @RequestBody CreateCommentRequest request,
      Authentication authentication) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(commentService.addComment(issueId, request, authentication.getName()));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping
  public ResponseEntity<?> getComments(@PathVariable Long issueId, Authentication authentication) {
    try {
      List<CommentResponse> comments =
          commentService.getComments(issueId, authentication.getName());

      return ResponseEntity.ok(comments);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<?> deleteComment(
      @PathVariable Long issueId, @PathVariable Long commentId, Authentication authentication) {
    try {
      commentService.deleteComment(issueId, commentId, authentication.getName());

      return ResponseEntity.ok(Map.of("message", "Comment deleted"));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
  }
}
