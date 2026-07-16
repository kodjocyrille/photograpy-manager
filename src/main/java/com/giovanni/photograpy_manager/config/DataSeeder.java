package com.giovanni.photograpy_manager.config;

import com.giovanni.photograpy_manager.domain.billing.ServiceCatalog;
import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.domain.user.User;
import com.giovanni.photograpy_manager.domain.user.UserRole;
import com.giovanni.photograpy_manager.repository.ServiceCatalogRepository;
import com.giovanni.photograpy_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServiceCatalogRepository catalogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCatalog();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("🌱 Initialisation des utilisateurs de test...");

            userRepository.save(User.builder()
                    .fullName("Admin PHOTOAGENCE")
                    .email("admin@photoagence.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build());

            userRepository.save(User.builder()
                    .fullName("Jean Photographe")
                    .email("photo@photoagence.com")
                    .passwordHash(passwordEncoder.encode("photo123"))
                    .role(UserRole.PHOTOGRAPHER)
                    .active(true)
                    .build());

            userRepository.save(User.builder()
                    .fullName("Marie Assistante")
                    .email("assistant@photoagence.com")
                    .passwordHash(passwordEncoder.encode("assistant123"))
                    .role(UserRole.ASSISTANT)
                    .active(true)
                    .build());

            log.info("✅ 3 utilisateurs de test créés (admin, photographe, assistant)");
        }
    }

    private void seedCatalog() {
        if (catalogRepository.count() == 0) {
            log.info("🌱 Initialisation du catalogue de prestations...");

            List<ServiceCatalog> prestations = List.of(
                // --- Mariage ---
                ServiceCatalog.builder()
                    .type(EventType.MARIAGE)
                    .description("Couverture complète mariage — Cérémonie + réception + 300 photos retouchées")
                    .minDurationHours(8)
                    .basePrice(new BigDecimal("350000"))
                    .build(),
                ServiceCatalog.builder()
                    .type(EventType.MARIAGE)
                    .description("Pack essentiel mariage — Cérémonie uniquement + 100 photos retouchées")
                    .minDurationHours(4)
                    .basePrice(new BigDecimal("150000"))
                    .build(),

                // --- Portrait ---
                ServiceCatalog.builder()
                    .type(EventType.PORTRAIT)
                    .description("Séance portrait studio — 10 photos retouchées HD + 2 tirages A4")
                    .minDurationHours(1)
                    .basePrice(new BigDecimal("50000"))
                    .build(),
                ServiceCatalog.builder()
                    .type(EventType.PORTRAIT)
                    .description("Séance portrait extérieur — 15 photos retouchées HD, lieu au choix")
                    .minDurationHours(2)
                    .basePrice(new BigDecimal("75000"))
                    .build(),

                // --- Corporate ---
                ServiceCatalog.builder()
                    .type(EventType.CORPORATE)
                    .description("Reportage corporate — Couverture événement entreprise + 50 photos")
                    .minDurationHours(4)
                    .basePrice(new BigDecimal("200000"))
                    .build(),
                ServiceCatalog.builder()
                    .type(EventType.CORPORATE)
                    .description("Portraits d'équipe — Headshots professionnels (jusqu'à 10 personnes)")
                    .minDurationHours(2)
                    .basePrice(new BigDecimal("120000"))
                    .build(),

                // --- Baptême ---
                ServiceCatalog.builder()
                    .type(EventType.BAPTEME)
                    .description("Couverture baptême / communion — Cérémonie + portraits famille + 80 photos")
                    .minDurationHours(3)
                    .basePrice(new BigDecimal("100000"))
                    .build(),

                // --- Grossesse ---
                ServiceCatalog.builder()
                    .type(EventType.GROSSESSE)
                    .description("Séance grossesse studio ou extérieur — 15 photos artistiques retouchées")
                    .minDurationHours(2)
                    .basePrice(new BigDecimal("65000"))
                    .build(),

                // --- Anniversaire ---
                ServiceCatalog.builder()
                    .type(EventType.ANNIVERSAIRE)
                    .description("Couverture anniversaire — Reportage complet + 60 photos retouchées")
                    .minDurationHours(3)
                    .basePrice(new BigDecimal("80000"))
                    .build(),

                // --- Événementiel ---
                ServiceCatalog.builder()
                    .type(EventType.EVENEMENTIEL)
                    .description("Reportage événementiel — Conférence, gala, soirée + galerie en ligne")
                    .minDurationHours(5)
                    .basePrice(new BigDecimal("250000"))
                    .build(),

                // --- Sur mesure ---
                ServiceCatalog.builder()
                    .type(EventType.SUR_MESURE)
                    .description("Prestation sur mesure — Tarif horaire, devis personnalisé selon besoins")
                    .minDurationHours(1)
                    .basePrice(new BigDecimal("40000"))
                    .build()
            );

            catalogRepository.saveAll(prestations);
            log.info("✅ {} prestations ajoutées au catalogue", prestations.size());
        }
    }
}
