package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.issue.CommentResponse;
import com.enterprise.project_management.dto.issue.CreateCommentRequest;
import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.IssueComment;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.IssueCommentRepository;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueCommentService {

  private final IssueCommentRepository commentRepository;
  private final IssueRepository issueRepository;
  private final UserRepository userRepository;
  private final ActivityService activityService;

  public IssueCommentService(
      IssueCommentRepository commentRepository,
      IssueRepository issueRepository,
      UserRepository userRepository,
      ActivityService activityService) {
    this.commentRepository = commentRepository;
    this.issueRepository = issueRepository;
    this.userRepository = userRepository;
    this.activityService = activityService;
  }

  @Transactional
  public CommentResponse addComment(Long issueId, CreateCommentRequest request, String email) {
    User user = getUser(email);

    Issue issue =
        issueRepository
            .findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

    checkIssueAccess(user, issue);

    IssueComment comment = new IssueComment();
    comment.setIssue(issue);
    comment.setUser(user);
    comment.setContent(request.getContent());

    CommentResponse response = CommentResponse.from(commentRepository.save(comment));
    activityService.record(
        email,
        issue.getProject(),
        issue,
        "COMMENT_ADDED",
        user.getName() + " commented on issue " + issue.getTitle());
    return response;
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> getComments(Long issueId, String email) {
    User user = getUser(email);

    Issue issue =
        issueRepository
            .findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

    checkIssueAccess(user, issue);

    return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId).stream()
        .map(CommentResponse::from)
        .toList();
  }

  @Transactional
  public void deleteComment(Long issueId, Long commentId, String email) {
    User user = getUser(email);

    Issue issue =
        issueRepository
            .findById(issueId)
            .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

    checkIssueAccess(user, issue);

    IssueComment comment =
        commentRepository
            .findByIdAndIssueId(commentId, issueId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

    boolean isAdmin = user.getRole() == Role.ADMIN;
    boolean isProjectManager =
        user.getRole() == Role.MANAGER
            && issue.getProject().getManager() != null
            && issue.getProject().getManager().getId().equals(user.getId());

    boolean isCommentOwner = comment.getUser().getId().equals(user.getId());

    if (!isAdmin && !isProjectManager && !isCommentOwner) {
      throw new AccessDeniedException("You can only delete your own comment");
    }

    commentRepository.delete(comment);
  }

  private User getUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private void checkIssueAccess(User user, Issue issue) {
    if (user.getRole() == Role.ADMIN) {
      return;
    }

    if (user.getRole() == Role.MANAGER
        && issue.getProject().getManager() != null
        && issue.getProject().getManager().getId().equals(user.getId())) {
      return;
    }

    if (user.getRole() == Role.EMPLOYEE
        && issue.getAssignedTo() != null
        && issue.getAssignedTo().getId().equals(user.getId())) {
      return;
    }

    throw new AccessDeniedException("You do not have access to this issue");
  }
}
