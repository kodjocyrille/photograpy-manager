package com.giovanni.photograpy_manager.domain.accounting;

import com.giovanni.photograpy_manager.domain.billing.Invoice;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_entries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccountingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type; // CREDIT (recette), DEBIT (dépense)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice; // Lien optionnel vers facture

    @Column(nullable = false)
    private String label; // Libellé de l'écriture

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount; // Montant en FCFA

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AccountCategory category;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
