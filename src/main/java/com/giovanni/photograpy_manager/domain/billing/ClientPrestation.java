package com.giovanni.photograpy_manager.domain.billing;

import com.giovanni.photograpy_manager.domain.client.Client;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_prestations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ClientPrestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_catalog_id", nullable = false)
    private ServiceCatalog serviceCatalog;

    /** Prix convenu (peut différer du prix catalogue) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PrestationStatus status = PrestationStatus.EN_ATTENTE;

    /** Date/heure planifiée de la prestation */
    private LocalDateTime scheduledDate;

    /** Date/heure fin (optionnel — calculé si non renseigné) */
    private LocalDateTime scheduledEndDate;

    private String notes;

    /** Facture générée à partir de cette prestation (nullable) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
    }
}
