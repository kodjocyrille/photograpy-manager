package com.giovanni.photograpy_manager.domain.accounting;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccountCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Ex : "Prestations photo", "Fournitures", "Déplacements"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type; // REVENUE, EXPENSE

    @Builder.Default
    private boolean active = true;
}
