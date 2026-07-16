package com.giovanni.photograpy_manager.domain.alert;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Configuration d'activation/désactivation par type d'alerte.
 */
@Entity
@Table(name = "alert_configs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AlertConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AlertType alertType;

    @Builder.Default
    private boolean enabled = true;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
