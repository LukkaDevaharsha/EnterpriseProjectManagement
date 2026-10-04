package com.enterprise.project_management.repository;

import com.enterprise.project_management.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findTop50ByRecipientIdOrderByCreatedAtDesc(Long recipientId);
  long countByRecipientIdAndReadAtIsNull(Long recipientId);
  Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);
  List<Notification> findByRecipientIdAndReadAtIsNull(Long recipientId);
}
