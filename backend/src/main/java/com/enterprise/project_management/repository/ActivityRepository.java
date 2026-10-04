package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.Activity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
  long countByActorId(Long userId);

  List<Activity> findTop100ByOrderByCreatedAtDesc();

  List<Activity> findTop100ByProjectIdOrderByCreatedAtDesc(Long projectId);

  List<Activity> findTop100ByIssueIdOrderByCreatedAtDesc(Long issueId);
}
