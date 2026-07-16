package com.giovanni.photograpy_manager.domain.billing;

import com.giovanni.photograpy_manager.domain.client.Client;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String quoteNumber; // DEV-2026-XXXX

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuoteLine> lines = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.DRAFT;

    private LocalDate validUntil;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void recalculateTotal() {
        this.totalAmount = lines.stream()
                .map(QuoteLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addLine(QuoteLine line) {
        lines.add(line);
        line.setQuote(this);
        recalculateTotal();
    }
}
