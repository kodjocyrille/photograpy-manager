package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.alert.AlertConfig;
import com.giovanni.photograpy_manager.domain.alert.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AlertConfigRepository extends JpaRepository<AlertConfig, Long> {
    Optional<AlertConfig> findByAlertType(AlertType alertType);
    List<AlertConfig> findAllByOrderByAlertTypeAsc();
}
