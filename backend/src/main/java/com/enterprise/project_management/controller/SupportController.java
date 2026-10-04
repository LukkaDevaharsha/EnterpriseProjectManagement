package com.enterprise.project_management.controller;

import com.enterprise.project_management.service.SupportMailer;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/support")
public class SupportController {
  private final SupportMailer support;
  public SupportController(SupportMailer support) { this.support = support; }
  @PostMapping @PreAuthorize("hasRole('MANAGER')") @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, String> send(@RequestBody Map<String, String> request, Authentication authentication) {
    support.send(request.get("subject"), request.get("details"), authentication.getName());
    return Map.of("message", "Support request sent to admin.");
  }

  public record ManagerContactRequest(@NotNull Long projectId, @NotBlank @Size(max = 150) String subject,
      @NotBlank @Size(max = 5000) String details) {}

  @PostMapping("/manager") @PreAuthorize("hasRole('EMPLOYEE')") @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, String> contactManager(@Valid @RequestBody ManagerContactRequest request, Authentication authentication) {
    support.sendToManager(request.projectId(), request.subject(), request.details(), authentication.getName());
    return Map.of("message", "Your message was emailed to the project manager.");
  }
}
