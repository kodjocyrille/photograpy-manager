package com.giovanni.photograpy_manager.repository;

import com.giovanni.photograpy_manager.domain.magiclink.MagicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MagicLinkRepository extends JpaRepository<MagicLink, Long> {
    Optional<MagicLink> findByToken(String token);
    List<MagicLink> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<MagicLink> findByDossierIdOrderByCreatedAtDesc(Long dossierId);
    List<MagicLink> findAllByOrderByCreatedAtDesc();
    List<MagicLink> findByRevokedFalseAndExpiresAtBefore(LocalDateTime now);
}
