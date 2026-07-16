package com.giovanni.photograpy_manager.domain.workspace;

import com.giovanni.photograpy_manager.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private Dossier dossier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DossierStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DossierStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }
}
