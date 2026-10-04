package com.enterprise.project_management.specification;

import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.IssuePriority;
import com.enterprise.project_management.entity.IssueStatus;
import org.springframework.data.jpa.domain.Specification;

public class IssueSpecification {

  private IssueSpecification() {}

  public static Specification<Issue> search(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) {
        return null;
      }

      String value = "%" + search.toLowerCase() + "%";

      return cb.or(
          cb.like(cb.lower(root.get("title")), value),
          cb.like(cb.lower(root.get("description")), value));
    };
  }

  public static Specification<Issue> status(IssueStatus status) {
    return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
  }

  public static Specification<Issue> priority(IssuePriority priority) {
    return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
  }

  public static Specification<Issue> projectId(Long projectId) {
    return (root, query, cb) ->
        projectId == null ? null : cb.equal(root.get("project").get("id"), projectId);
  }

  public static Specification<Issue> assignedTo(Long userId) {
    return (root, query, cb) ->
        userId == null ? null : cb.equal(root.get("assignedTo").get("id"), userId);
  }

  public static Specification<Issue> managerId(Long managerId) {
    return (root, query, cb) ->
        managerId == null
            ? null
            : cb.equal(root.get("project").get("manager").get("id"), managerId);
  }
}
