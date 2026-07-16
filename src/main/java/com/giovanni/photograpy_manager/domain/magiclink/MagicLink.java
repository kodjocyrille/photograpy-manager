package com.giovanni.photograpy_manager.domain.magiclink;

import com.giovanni.photograpy_manager.domain.client.Client;
import com.giovanni.photograpy_manager.domain.workspace.Dossier;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "magic_links")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MagicLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id")
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, unique = true)
    @Builder.Default
    private String token = UUID.randomUUID().toString();

    private String pinCode; // Optionnel, 4-6 chiffres

    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "magicLink", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MagicLinkAsset> assets = new ArrayList<>();

    @OneToMany(mappedBy = "magicLink", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("accessedAt DESC")
    private List<AccessLog> accessLogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (token == null) token = UUID.randomUUID().toString();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isAccessible() {
        return !revoked && !isExpired();
    }
}
