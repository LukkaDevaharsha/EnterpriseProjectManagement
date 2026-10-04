package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  Optional<Project> findByName(String name);

  boolean existsByName(String name);

  List<Project> findByManagerId(Long managerId);
}
