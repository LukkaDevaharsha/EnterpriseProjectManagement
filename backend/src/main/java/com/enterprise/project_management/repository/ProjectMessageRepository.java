package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.ProjectMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, Long> {
  List<ProjectMessage> findTop100ByProjectIdOrderByCreatedAtAsc(Long projectId);
  long countByProjectIdAndSenderIdNotAndIdGreaterThan(Long projectId, Long senderId, Long id);
  @org.springframework.data.jpa.repository.Query("select coalesce(max(message.id), 0) from ProjectMessage message where message.project.id = :projectId")
  Long findLatestIdByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
}
