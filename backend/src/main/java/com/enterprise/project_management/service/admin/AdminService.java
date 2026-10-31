package com.enterprise.project_management.service.admin;

import com.enterprise.project_management.dto.admin.AdminCreateUserRequest;
import com.enterprise.project_management.dto.admin.AdminUserResponse;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.repository.ActivityRepository;
import com.enterprise.project_management.repository.IssueCommentRepository;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@Service
public class AdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ProjectMemberRepository projectMemberRepository;
  private final IssueRepository issueRepository;
  private final IssueCommentRepository issueCommentRepository;
  private final ActivityRepository activityRepository;

  public AdminService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      ProjectMemberRepository projectMemberRepository,
      IssueRepository issueRepository,
      IssueCommentRepository issueCommentRepository,
      ActivityRepository activityRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.projectMemberRepository = projectMemberRepository;
    this.issueRepository = issueRepository;
    this.issueCommentRepository = issueCommentRepository;
    this.activityRepository = activityRepository;
  }

  public List<AdminUserResponse> getAllUsers() {
    return userRepository.findAll().stream().map(AdminUserResponse::from).toList();
  }

  public AdminUserResponse createUser(AdminCreateUserRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already registered");
    }

    if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
      throw new IllegalArgumentException("Employee ID already registered");
    }

    if (request.getRole() == null) {
      throw new IllegalArgumentException("Role is required");
    }

    User user = new User();

    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEmployeeId(request.getEmployeeId());
    user.setDepartment(request.getDepartment());
    user.setRole(request.getRole());
    user.setActive(true);
    user.setEmailVerified(true);

    User savedUser = userRepository.save(user);

    return AdminUserResponse.from(savedUser);
  }

  @Transactional
  public AdminUserResponse updateRole(
      Long userId, com.enterprise.project_management.dto.admin.UpdateRoleRequest request, String actorEmail) {

    User user = getUser(userId);

    if (request.getRole() == null) {
      throw new IllegalArgumentException("Role is required");
    }

    if (isActor(user, actorEmail)) {
      throw new AccessDeniedException("You cannot change your own administrator role or privileges.");
    }
    if (user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
      ensureAnotherActiveAdmin();
    }

    user.setRole(request.getRole());

    return AdminUserResponse.from(userRepository.save(user));
  }

  @Transactional
  public AdminUserResponse updateStatus(
      Long userId, com.enterprise.project_management.dto.admin.UpdateUserStatusRequest request, String actorEmail) {

    User user = getUser(userId);

    if (!request.isActive() && isActor(user, actorEmail)) {
      throw new AccessDeniedException("You cannot deactivate your own account.");
    }
    if (!request.isActive() && user.getRole() == Role.ADMIN) {
      ensureAnotherActiveAdmin();
    }

    user.setActive(request.isActive());

    return AdminUserResponse.from(userRepository.save(user));
  }

  @Transactional
  public void deleteUser(Long userId, String actorEmail) {

    User user = getUser(userId);

    if (isActor(user, actorEmail)) {
      throw new AccessDeniedException("You cannot delete your own account.");
    }
    if (user.getRole() == Role.ADMIN && user.isActive()) {
      ensureAnotherActiveAdmin();
    }

    long projectMemberships = projectMemberRepository.countByUserId(userId);

    long assignedIssues = issueRepository.countByAssignedToId(userId);

    long createdIssues = issueRepository.countByCreatedById(userId);

    long comments = issueCommentRepository.countByUserId(userId);
    long activities = activityRepository.countByActorId(userId);

    if (projectMemberships > 0) {
      throw new IllegalArgumentException(
          "Cannot delete user because the user belongs to one or more projects");
    }

    if (assignedIssues > 0) {
      throw new IllegalArgumentException(
          "Cannot delete user because issues are assigned to this user");
    }

    if (createdIssues > 0) {
      throw new IllegalArgumentException("Cannot delete user because this user created issues");
    }

    if (comments > 0) {
      throw new IllegalArgumentException("Cannot delete user because this user has issue comments");
    }

    if (activities > 0) {
      throw new IllegalArgumentException(
          "Cannot delete user because the user has activity history. Deactivate the account instead.");
    }

    userRepository.delete(user);
  }

  private User getUser(Long userId) {

    return userRepository
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private boolean isActor(User target, String actorEmail) {
    return target.getEmail().equalsIgnoreCase(actorEmail);
  }

  private void ensureAnotherActiveAdmin() {
    if (userRepository.lockActiveUsersByRole(Role.ADMIN).size() <= 1) {
      throw new IllegalStateException("The last active administrator cannot be deactivated, deleted, or demoted.");
    }
  }
}
