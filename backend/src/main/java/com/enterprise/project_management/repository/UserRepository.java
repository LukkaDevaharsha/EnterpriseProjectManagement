package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.entity.Role;
import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByEmployeeId(String employeeId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.role = :role and u.active = true")
  List<User> lockActiveUsersByRole(@Param("role") Role role);
}
