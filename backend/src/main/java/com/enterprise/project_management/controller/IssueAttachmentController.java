package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.attachment.AttachmentResponse;
import com.enterprise.project_management.entity.Attachment;
import com.enterprise.project_management.service.IssueAttachmentService;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/issues/{issueId}/attachments")
@PreAuthorize("isAuthenticated()")
public class IssueAttachmentController {
  private final IssueAttachmentService service;

  public IssueAttachmentController(IssueAttachmentService service) {
    this.service = service;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public AttachmentResponse upload(
      @PathVariable Long issueId, @RequestParam("file") MultipartFile file, Authentication auth) {
    return service.upload(issueId, file, auth.getName());
  }

  @GetMapping
  public List<AttachmentResponse> list(@PathVariable Long issueId, Authentication auth) {
    return service.list(issueId, auth.getName());
  }

  @GetMapping("/{attachmentId}")
  public ResponseEntity<Resource> download(
      @PathVariable Long issueId, @PathVariable Long attachmentId, Authentication auth) {
    Attachment a = service.download(issueId, attachmentId, auth.getName());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(a.getOriginalName()).build().toString())
        .body(service.resource(a));
  }

  @DeleteMapping("/{attachmentId}")
  public ResponseEntity<Void> delete(
      @PathVariable Long issueId, @PathVariable Long attachmentId, Authentication auth) {
    service.delete(issueId, attachmentId, auth.getName());
    return ResponseEntity.noContent().build();
  }
}
