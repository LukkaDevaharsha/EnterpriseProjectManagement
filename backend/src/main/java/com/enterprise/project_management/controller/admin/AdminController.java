package com.enterprise.project_management.controller.admin;

import com.enterprise.project_management.dto.admin.AdminCreateUserRequest;
import com.enterprise.project_management.dto.admin.AdminUserResponse;
import com.enterprise.project_management.dto.admin.UpdateRoleRequest;
import com.enterprise.project_management.dto.admin.UpdateUserStatusRequest;
import com.enterprise.project_management.service.admin.AdminService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/users")
  public List<AdminUserResponse> getAllUsers() {
    return adminService.getAllUsers();
  }

  @PostMapping("/users")
  public AdminUserResponse createUser(@RequestBody AdminCreateUserRequest request) {
    return adminService.createUser(request);
  }

  @PatchMapping("/users/{id}/role")
  public AdminUserResponse updateRole(
      @PathVariable Long id, @RequestBody UpdateRoleRequest request) {
    return adminService.updateRole(id, request);
  }

  @PatchMapping("/users/{id}/status")
  public AdminUserResponse updateStatus(
      @PathVariable Long id, @RequestBody UpdateUserStatusRequest request) {
    return adminService.updateStatus(id, request);
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    try {
      adminService.deleteUser(id);
      return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }
  }
}
