package com.giovanni.photograpy_manager.domain.alert;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Historique des emails envoyés par le système.
 */
@Entity
@Table(name = "email_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Column(nullable = false)
    private String recipientEmail;

    private String recipientName;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String bodyPreview; // First 500 chars of the email

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) sentAt = LocalDateTime.now();
    }
}
