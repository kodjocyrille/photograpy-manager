package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.alert.*;
import com.giovanni.photograpy_manager.repository.AlertConfigRepository;
import com.giovanni.photograpy_manager.repository.EmailLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertConfigRepository alertConfigRepository;
    private final EmailLogRepository emailLogRepository;

    /**
     * Initialize alert configs for all types if not present.
     */
    @PostConstruct
    @Transactional
    public void initConfigs() {
        for (AlertType type : AlertType.values()) {
            if (alertConfigRepository.findByAlertType(type).isEmpty()) {
                alertConfigRepository.save(AlertConfig.builder()
                        .alertType(type).enabled(true).build());
            }
        }
    }

    public List<AlertConfig> findAllConfigs() {
        return alertConfigRepository.findAllByOrderByAlertTypeAsc();
    }

    @Transactional
    public void toggleAlert(Long id) {
        AlertConfig config = alertConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config introuvable"));
        config.setEnabled(!config.isEnabled());
        alertConfigRepository.save(config);
    }

    public boolean isEnabled(AlertType type) {
        return alertConfigRepository.findByAlertType(type)
                .map(AlertConfig::isEnabled).orElse(true);
    }

    // --- Email Logs ---
    public List<EmailLog> findAllLogs() {
        return emailLogRepository.findAllByOrderBySentAtDesc();
    }

    public long countSent() { return emailLogRepository.countByStatus(EmailStatus.SENT); }
    public long countFailed() { return emailLogRepository.countByStatus(EmailStatus.FAILED); }
}
