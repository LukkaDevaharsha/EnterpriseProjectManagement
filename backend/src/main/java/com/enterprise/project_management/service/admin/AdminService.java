package com.enterprise.project_management.service.admin;

import com.enterprise.project_management.dto.admin.AdminCreateUserRequest;
import com.enterprise.project_management.dto.admin.AdminUserResponse;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.ActivityRepository;
import com.enterprise.project_management.repository.IssueCommentRepository;
import com.enterprise.project_management.repository.IssueRepository;
import com.enterprise.project_management.repository.ProjectMemberRepository;
import com.enterprise.project_management.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  public AdminUserResponse updateRole(
      Long userId, com.enterprise.project_management.dto.admin.UpdateRoleRequest request) {

    User user = getUser(userId);

    if (request.getRole() == null) {
      throw new IllegalArgumentException("Role is required");
    }

    user.setRole(request.getRole());

    return AdminUserResponse.from(userRepository.save(user));
  }

  public AdminUserResponse updateStatus(
      Long userId, com.enterprise.project_management.dto.admin.UpdateUserStatusRequest request) {

    User user = getUser(userId);

    user.setActive(request.isActive());

    return AdminUserResponse.from(userRepository.save(user));
  }

  @Transactional
  public void deleteUser(Long userId) {

    User user = getUser(userId);

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
}
