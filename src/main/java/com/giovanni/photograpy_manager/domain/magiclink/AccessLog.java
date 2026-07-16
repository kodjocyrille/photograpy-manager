package com.giovanni.photograpy_manager.domain.magiclink;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magic_link_id", nullable = false)
    private MagicLink magicLink;

    private String ipAddress;

    @Column(nullable = false)
    private String action; // VIEW, DOWNLOAD_SINGLE, DOWNLOAD_ALL

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime accessedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (accessedAt == null) accessedAt = LocalDateTime.now();
    }
}
