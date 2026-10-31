package com.enterprise.project_management.service.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.enterprise.project_management.dto.admin.UpdateRoleRequest;
import com.enterprise.project_management.dto.admin.UpdateUserStatusRequest;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.ActivityRepository;
import com.enterprise.project_management.repository.IssueCommentRepository;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock ProjectMemberRepository projectMemberRepository;
  @Mock IssueRepository issueRepository;
  @Mock IssueCommentRepository issueCommentRepository;
  @Mock ActivityRepository activityRepository;
  @InjectMocks AdminService adminService;

  private User admin;

  @BeforeEach
  void setUp() {
    admin = new User();
    admin.setEmail("admin@example.com");
    admin.setRole(Role.ADMIN);
    admin.setActive(true);
    Mockito.lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
  }

  @Test
  void adminCannotDeactivateSelf() {
    UpdateUserStatusRequest request = new UpdateUserStatusRequest();
    request.setActive(false);
    assertThrows(AccessDeniedException.class,
        () -> adminService.updateStatus(1L, request, "admin@example.com"));
  }

  @Test
  void adminCannotDeleteSelf() {
    assertThrows(AccessDeniedException.class,
        () -> adminService.deleteUser(1L, "admin@example.com"));
  }

  @Test
  void adminCannotDemoteSelf() {
    UpdateRoleRequest request = new UpdateRoleRequest();
    request.setRole(Role.MANAGER);
    assertThrows(AccessDeniedException.class,
        () -> adminService.updateRole(1L, request, "admin@example.com"));
  }

  @Test
  void lastActiveAdminCannotBeDeactivatedDemotedOrDeleted() {
    when(userRepository.lockActiveUsersByRole(Role.ADMIN)).thenReturn(List.of(admin));
    UpdateUserStatusRequest status = new UpdateUserStatusRequest();
    status.setActive(false);
    UpdateRoleRequest role = new UpdateRoleRequest();
    role.setRole(Role.EMPLOYEE);

    assertThrows(IllegalStateException.class, () -> adminService.updateStatus(1L, status, "other@example.com"));
    assertThrows(IllegalStateException.class, () -> adminService.updateRole(1L, role, "other@example.com"));
    assertThrows(IllegalStateException.class, () -> adminService.deleteUser(1L, "other@example.com"));
  }

  @Test
  void adminCanDeactivateAnotherEmployee() {
    User employee = new User();
    employee.setEmail("employee@example.com");
    employee.setRole(Role.EMPLOYEE);
    employee.setActive(true);
    when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
    when(userRepository.save(employee)).thenReturn(employee);
    UpdateUserStatusRequest request = new UpdateUserStatusRequest();
    request.setActive(false);

    assertDoesNotThrow(() -> adminService.updateStatus(2L, request, "admin@example.com"));
    verify(userRepository).save(employee);
  }
}
