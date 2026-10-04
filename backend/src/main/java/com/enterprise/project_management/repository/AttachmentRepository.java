package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.Attachment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
  List<Attachment> findByIssueIdOrderByCreatedAtDesc(Long issueId);

  Optional<Attachment> findByIdAndIssueId(Long id, Long issueId);
}
