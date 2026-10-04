package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.ProjectChatRead;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectChatReadRepository extends JpaRepository<ProjectChatRead, Long> {
  Optional<ProjectChatRead> findByProjectIdAndUserId(Long projectId, Long userId);
}
