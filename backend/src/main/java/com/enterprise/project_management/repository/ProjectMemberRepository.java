package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.ProjectMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

  boolean existsByProjectIdAndUserId(Long projectId, Long userId);

  List<ProjectMember> findByProjectId(Long projectId);

  long countByProjectId(Long projectId);

  List<ProjectMember> findByUserId(Long userId);

  void deleteByProjectIdAndUserId(Long projectId, Long userId);

  long countByUserId(Long userId);
}
