package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.Issue;
import com.enterprise.project_management.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IssueRepository
    extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {

  java.util.List<Issue> findByProjectIdOrderByCreatedAtAscIdAsc(Long projectId);

  long countByAssignedToId(Long userId);

  long countByAssignedToIdAndStatus(Long userId, IssueStatus status);

  long countByCreatedById(Long userId);

  long countByStatus(IssueStatus status);

  long countByProjectId(Long projectId);

  long countByProjectIdAndStatus(Long projectId, IssueStatus status);

  long countByProjectIdAndStatusIn(Long projectId, java.util.Collection<IssueStatus> statuses);
}
