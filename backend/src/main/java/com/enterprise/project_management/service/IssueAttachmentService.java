package com.enterprise.project_management.service;

import com.enterprise.project_management.dto.attachment.AttachmentResponse;
import com.enterprise.project_management.entity.*;
import com.enterprise.project_management.repository.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IssueAttachmentService {
  private static final long MAX_SIZE = 10L * 1024 * 1024;
  private static final Set<String> EXTENSIONS =
      Set.of("pdf", "png", "jpg", "jpeg", "doc", "docx", "xls", "xlsx", "txt", "zip");
  private final AttachmentRepository attachments;
  private final IssueRepository issues;
  private final UserRepository users;
  private final ProjectMemberRepository members;
  private final ActivityService activities;

  public IssueAttachmentService(
      AttachmentRepository attachments,
      IssueRepository issues,
      UserRepository users,
      ProjectMemberRepository members,
      ActivityService activities) {
    this.attachments = attachments;
    this.issues = issues;
    this.users = users;
    this.members = members;
    this.activities = activities;
  }

  @Transactional
  public AttachmentResponse upload(Long issueId, MultipartFile file, String email) {
    Issue issue = getIssue(issueId);
    User user = getUser(email);
    checkAccess(user, issue);
    if (file == null || file.isEmpty()) throw new IllegalArgumentException("File cannot be empty");
    if (file.getSize() > MAX_SIZE)
      throw new IllegalArgumentException("Attachment exceeds the 10 MB limit");
    String original =
        Path.of(Objects.requireNonNullElse(file.getOriginalFilename(), "file"))
            .getFileName()
            .toString();
    String ext =
        original.contains(".")
            ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
            : "";
    if (!EXTENSIONS.contains(ext)) throw new IllegalArgumentException("Unsupported file type");
    try {
      Path folder = Path.of("uploads", "issues").toAbsolutePath().normalize();
      Files.createDirectories(folder);
      String stored = UUID.randomUUID() + "." + ext;
      Path path = folder.resolve(stored).normalize();
      if (!path.startsWith(folder)) throw new IllegalArgumentException("Invalid file name");
      file.transferTo(path);
      Attachment saved =
          attachments.save(
              new Attachment(
                  issue,
                  user,
                  original,
                  stored,
                  file.getContentType(),
                  file.getSize(),
                  path.toString()));
      activities.record(
          email,
          issue.getProject(),
          issue,
          "ATTACHMENT_UPLOADED",
          user.getName() + " uploaded attachment " + original);
      return AttachmentResponse.from(saved);
    } catch (IOException e) {
      throw new IllegalStateException("Could not store attachment", e);
    }
  }

  @Transactional(readOnly = true)
  public List<AttachmentResponse> list(Long issueId, String email) {
    Issue issue = getIssue(issueId);
    checkAccess(getUser(email), issue);
    return attachments.findByIssueIdOrderByCreatedAtDesc(issueId).stream()
        .map(AttachmentResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public Attachment download(Long issueId, Long attachmentId, String email) {
    Issue issue = getIssue(issueId);
    checkAccess(getUser(email), issue);
    return attachments
        .findByIdAndIssueId(attachmentId, issueId)
        .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
  }

  @Transactional
  public void delete(Long issueId, Long attachmentId, String email) {
    User user = getUser(email);
    Issue issue = getIssue(issueId);
    checkAccess(user, issue);
    Attachment a =
        attachments
            .findByIdAndIssueId(attachmentId, issueId)
            .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
    boolean manager =
        user.getRole() == Role.MANAGER
            && issue.getProject().getManager() != null
            && issue.getProject().getManager().getId().equals(user.getId());
    if (user.getRole() != Role.ADMIN && !manager && !a.getUploadedBy().getId().equals(user.getId()))
      throw new AccessDeniedException("You cannot delete this attachment");
    try {
      Files.deleteIfExists(Path.of(a.getFilePath()));
    } catch (IOException e) {
      throw new IllegalStateException("Could not delete attachment file", e);
    }
    attachments.delete(a);
    activities.record(
        email,
        issue.getProject(),
        issue,
        "ATTACHMENT_DELETED",
        user.getName() + " deleted attachment " + a.getOriginalName());
  }

  public Resource resource(Attachment a) {
    Path root = Path.of("uploads", "issues").toAbsolutePath().normalize();
    Path file = Path.of(a.getFilePath()).toAbsolutePath().normalize();
    if (!file.startsWith(root)) throw new AccessDeniedException("Invalid attachment path");
    return new FileSystemResource(file);
  }

  private Issue getIssue(Long id) {
    return issues.findById(id).orElseThrow(() -> new IllegalArgumentException("Issue not found"));
  }

  private User getUser(String email) {
    return users
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private void checkAccess(User u, Issue i) {
    if (u.getRole() == Role.ADMIN) return;
    if (u.getRole() == Role.MANAGER
        && i.getProject().getManager() != null
        && i.getProject().getManager().getId().equals(u.getId())) return;
    if (u.getRole() == Role.EMPLOYEE
        && i.getAssignedTo() != null
        && i.getAssignedTo().getId().equals(u.getId())) return;
    throw new AccessDeniedException("You do not have access to this issue");
  }
}
