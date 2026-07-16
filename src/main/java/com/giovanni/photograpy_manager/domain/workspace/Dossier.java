package com.giovanni.photograpy_manager.domain.workspace;

import com.giovanni.photograpy_manager.domain.billing.Invoice;
import com.giovanni.photograpy_manager.domain.client.Client;
import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossiers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dossierCode; // DOS-2026-XXXX

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType serviceType;

    private LocalDate sessionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private User photographer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DossierStatus status = DossierStatus.WAITING_SESSION;

    @Enumerated(EnumType.STRING)
    @Column(name = "kanban_column", nullable = false)
    @Builder.Default
    private DossierColumn column = DossierColumn.TO_PROCESS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("createdAt DESC")
    private List<DossierComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("changedAt DESC")
    private List<StatusHistory> statusHistory = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(columnDefinition = "TEXT")
    private String checklist; // JSON checklist

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
