package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.magiclink.AccessLog;
import com.giovanni.photograpy_manager.domain.magiclink.MagicLink;
import com.giovanni.photograpy_manager.repository.ClientRepository;
import com.giovanni.photograpy_manager.repository.MagicLinkRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MagicLinkService {

    private final MagicLinkRepository magicLinkRepository;
    private final ClientRepository clientRepository;

    public List<MagicLink> findAll() {
        return magicLinkRepository.findAllByOrderByCreatedAtDesc();
    }

    public MagicLink findById(Long id) {
        return magicLinkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lien magique introuvable : " + id));
    }

    public MagicLink findByToken(String token) {
        return magicLinkRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Lien invalide ou expiré"));
    }

    @Transactional
    public MagicLink create(Long clientId, Long dossierId, String pinCode, int expirationDays) {
        MagicLink link = MagicLink.builder()
                .client(clientRepository.findById(clientId)
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable")))
                .pinCode(pinCode != null && !pinCode.isBlank() ? pinCode : null)
                .expiresAt(LocalDateTime.now().plusDays(expirationDays > 0 ? expirationDays : 7))
                .build();

        return magicLinkRepository.save(link);
    }

    @Transactional
    public void revoke(Long id) {
        MagicLink link = findById(id);
        link.setRevoked(true);
        magicLinkRepository.save(link);
    }

    @Transactional
    public void logAccess(MagicLink link, String ipAddress, String action) {
        AccessLog log = AccessLog.builder()
                .magicLink(link)
                .ipAddress(ipAddress)
                .action(action)
                .build();
        link.getAccessLogs().add(log);
        magicLinkRepository.save(link);
    }

    /**
     * Auto-expire les liens dépassés (toutes les heures)
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void autoExpireLinks() {
        List<MagicLink> expired = magicLinkRepository
                .findByRevokedFalseAndExpiresAtBefore(LocalDateTime.now());
        for (MagicLink link : expired) {
            link.setRevoked(true);
        }
        if (!expired.isEmpty()) {
            magicLinkRepository.saveAll(expired);
        }
    }
}
