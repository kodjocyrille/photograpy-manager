package com.giovanni.photograpy_manager.domain.magiclink;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "magic_link_assets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MagicLinkAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magic_link_id", nullable = false)
    private MagicLink magicLink;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String s3Key; // Clé S3/MinIO

    private String contentType;

    private long fileSize;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
    }
}
