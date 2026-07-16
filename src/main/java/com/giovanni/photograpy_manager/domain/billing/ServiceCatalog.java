package com.giovanni.photograpy_manager.domain.billing;

import com.giovanni.photograpy_manager.domain.client.EventType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "service_catalog")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ServiceCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private String description;

    private int minDurationHours;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Builder.Default
    private boolean active = true;
}
