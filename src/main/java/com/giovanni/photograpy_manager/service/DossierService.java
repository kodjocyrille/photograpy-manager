package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.client.Client;
import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.domain.user.User;
import com.giovanni.photograpy_manager.domain.workspace.*;
import com.giovanni.photograpy_manager.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DossierService {

    private final DossierRepository dossierRepository;
    private final DossierCommentRepository commentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public List<Dossier> findAll() {
        return dossierRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Dossier findById(Long id) {
        Dossier dossier = dossierRepository.findByIdWithComments(id)
                .orElseThrow(() -> new EntityNotFoundException("Dossier introuvable : " + id));
        // Second fetch to load statusHistory (avoids MultipleBagFetchException)
        dossierRepository.findByIdWithHistory(id);
        return dossier;
    }

    public List<Dossier> findByColumn(DossierColumn column) {
        return dossierRepository.findByColumnOrderByPriorityDescCreatedAtDesc(column);
    }

    public List<Dossier> findFiltered(DossierStatus status, Priority priority, Long photographerId) {
        return dossierRepository.findFiltered(status, priority, photographerId);
    }

    public long countByColumn(DossierColumn column) {
        return dossierRepository.countByColumn(column);
    }

    public List<Dossier> findByPhotographer(Long photographerId) {
        return dossierRepository.findByPhotographerIdWithClient(photographerId);
    }

    @Transactional
    public Dossier create(Long clientId, EventType serviceType, LocalDate sessionDate,
                          Long photographerId, Priority priority) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable"));

        Dossier dossier = Dossier.builder()
                .dossierCode(generateCode())
                .client(client)
                .serviceType(serviceType)
                .sessionDate(sessionDate)
                .priority(priority != null ? priority : Priority.NORMAL)
                .build();

        if (photographerId != null) {
            dossier.setPhotographer(userRepository.findById(photographerId).orElse(null));
        }

        return dossierRepository.save(dossier);
    }

    @Transactional
    public Dossier updateStatus(Long id, DossierStatus newStatus, User changedBy) {
        Dossier dossier = findById(id);
        DossierStatus oldStatus = dossier.getStatus();

        // Track history
        StatusHistory history = StatusHistory.builder()
                .dossier(dossier)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .changedBy(changedBy)
                .build();
        dossier.getStatusHistory().add(history);
        dossier.setStatus(newStatus);

        // Auto-move to DONE column when completed
        if (newStatus == DossierStatus.COMPLETED) {
            dossier.setColumn(DossierColumn.DONE);
        }

        return dossierRepository.save(dossier);
    }

    @Transactional
    public Dossier moveColumn(Long id, DossierColumn column) {
        Dossier dossier = findById(id);
        dossier.setColumn(column);
        return dossierRepository.save(dossier);
    }

    @Transactional
    public DossierComment addComment(Long dossierId, User author, String content) {
        Dossier dossier = findById(dossierId);
        DossierComment comment = DossierComment.builder()
                .dossier(dossier)
                .author(author)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }

    private String generateCode() {
        long count = dossierRepository.count() + 1;
        return String.format("DOS-%d-%04d", Year.now().getValue(), count);
    }
}
