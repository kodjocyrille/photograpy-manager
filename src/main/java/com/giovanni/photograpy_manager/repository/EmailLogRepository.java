package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.alert.EmailLog;
import com.giovanni.photograpy_manager.domain.alert.AlertType;
import com.giovanni.photograpy_manager.domain.alert.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findAllByOrderBySentAtDesc();
    List<EmailLog> findByAlertTypeOrderBySentAtDesc(AlertType type);
    List<EmailLog> findByStatusOrderBySentAtDesc(EmailStatus status);
    long countByStatus(EmailStatus status);
}
