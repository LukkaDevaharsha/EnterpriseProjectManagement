package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.IssueComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

  List<IssueComment> findByIssueIdOrderByCreatedAtAsc(Long issueId);

  Optional<IssueComment> findByIdAndIssueId(Long id, Long issueId);

  long countByUserId(Long userId);
}
