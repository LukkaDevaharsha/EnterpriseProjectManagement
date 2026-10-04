package com.enterprise.project_management.controller;

import com.enterprise.project_management.dto.admin.AdminUserResponse;
import com.enterprise.project_management.dto.user.*;
import com.enterprise.project_management.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public Map<String, Object> getCurrentUser(Authentication authentication) {
    var profile = userService.getProfile(authentication.getName());
    return Map.of(
        "id",
        profile.id(),
        "name",
        profile.name(),
        "email",
        profile.email(),
        "employeeId",
        profile.employeeId(),
        "department",
        profile.department(),
        "role",
        profile.role(),
        "active",
        profile.active(),
        "createdAt",
        profile.createdAt(),
        "authorities",
        authentication.getAuthorities());
  }

  @PutMapping("/me")
  public UserProfileResponse updateProfile(
      Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
    return userService.updateProfile(authentication.getName(), request);
  }

  @PatchMapping("/me/password")
  public Map<String, String> changePassword(
      Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
    userService.changePassword(authentication.getName(), request);
    return Map.of("message", "Password updated successfully");
  }

  @GetMapping("/employees")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public List<AdminUserResponse> getEmployees() {
    return userService.getEmployees();
  }
}
