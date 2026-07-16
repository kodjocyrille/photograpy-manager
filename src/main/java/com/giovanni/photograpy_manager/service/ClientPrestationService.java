package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.billing.*;
import com.giovanni.photograpy_manager.domain.client.Client;
import com.giovanni.photograpy_manager.repository.ClientPrestationRepository;
import com.giovanni.photograpy_manager.repository.ServiceCatalogRepository;
import com.giovanni.photograpy_manager.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientPrestationService {

    private final ClientPrestationRepository prestationRepository;
    private final ClientRepository clientRepository;
    private final ServiceCatalogRepository catalogRepository;
    private final InvoiceService invoiceService;
    private final AccountingService accountingService;

    public List<ClientPrestation> findByClient(Long clientId) {
        return prestationRepository.findByClientIdOrderByAssignedAtDesc(clientId);
    }

    public List<ClientPrestation> findByService(Long serviceId) {
        return prestationRepository.findByServiceCatalogIdOrderByAssignedAtDesc(serviceId);
    }

    public List<ClientPrestation> findAll() {
        return prestationRepository.findAll();
    }

    public ClientPrestation findById(Long id) {
        return prestationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignation introuvable : " + id));
    }

    public List<ClientPrestation> findByDateRange(LocalDate start, LocalDate end) {
        return prestationRepository.findByScheduledDateBetween(
                start.atStartOfDay(), end.atTime(23, 59));
    }

    public List<ClientPrestation> findByScheduledDate(LocalDate date) {
        return prestationRepository.findByScheduledDate(date);
    }

    @Transactional
    public ClientPrestation assign(Long clientId, Long serviceCatalogId, BigDecimal agreedPrice,
                                    LocalDateTime scheduledDate, LocalDateTime scheduledEndDate,
                                    String notes) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable : " + clientId));
        ServiceCatalog service = catalogRepository.findById(serviceCatalogId)
                .orElseThrow(() -> new EntityNotFoundException("Prestation introuvable : " + serviceCatalogId));

        // Si pas de prix convenu, utiliser le prix catalogue
        BigDecimal price = agreedPrice != null ? agreedPrice : service.getBasePrice();

        // Si pas de date fin, calculer à partir de la durée min du catalogue
        if (scheduledDate != null && scheduledEndDate == null) {
            scheduledEndDate = scheduledDate.plusHours(service.getMinDurationHours());
        }

        ClientPrestation cp = ClientPrestation.builder()
                .client(client)
                .serviceCatalog(service)
                .agreedPrice(price)
                .scheduledDate(scheduledDate)
                .scheduledEndDate(scheduledEndDate)
                .notes(notes)
                .build();
        return prestationRepository.save(cp);
    }

    @Transactional
    public void updateStatus(Long id, PrestationStatus status) {
        ClientPrestation cp = prestationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignation introuvable : " + id));
        PrestationStatus oldStatus = cp.getStatus();
        cp.setStatus(status);

        // Auto-generate invoice when switching to EN_COURS (only if not already invoiced)
        if (status == PrestationStatus.EN_COURS && oldStatus != PrestationStatus.EN_COURS && cp.getInvoice() == null) {
            InvoiceLine line = InvoiceLine.builder()
                    .description(cp.getServiceCatalog().getType().getLabel() + " — " + cp.getServiceCatalog().getDescription())
                    .quantity(1)
                    .unitPrice(cp.getAgreedPrice())
                    .build();

            Invoice invoice = invoiceService.createDirect(
                    cp.getClient().getId(),
                    List.of(line),
                    java.time.LocalDate.now().plusDays(30)
            );
            // Mark as SENT (awaiting payment = impayé)
            invoice.setStatus(InvoiceStatus.SENT);

            cp.setInvoice(invoice);

            // Create accounting entry for the receivable
            accountingService.createDebitFromPrestation(cp, invoice);
        }

        prestationRepository.save(cp);
    }

    /**
     * Génère une facture à partir d'une prestation terminée
     */
    @Transactional
    public Invoice generateInvoice(Long prestationId) {
        ClientPrestation cp = findById(prestationId);
        if (cp.getInvoice() != null) {
            throw new IllegalStateException("Une facture a déjà été générée pour cette prestation");
        }

        InvoiceLine line = InvoiceLine.builder()
                .description(cp.getServiceCatalog().getType().getLabel() + " — " + cp.getServiceCatalog().getDescription())
                .quantity(1)
                .unitPrice(cp.getAgreedPrice())
                .build();

        Invoice invoice = invoiceService.createDirect(
                cp.getClient().getId(),
                List.of(line),
                java.time.LocalDate.now().plusDays(30)
        );

        cp.setInvoice(invoice);
        prestationRepository.save(cp);
        return invoice;
    }

    /**
     * Sync: generate invoices for existing EN_COURS prestations that don't have one yet.
     * Handles pre-existing data that was set to EN_COURS before the auto-invoice logic was added.
     */
    @Transactional
    public int syncEnCoursInvoices() {
        List<ClientPrestation> unsynced = prestationRepository.findEnCoursWithoutInvoice();
        for (ClientPrestation cp : unsynced) {
            InvoiceLine line = InvoiceLine.builder()
                    .description(cp.getServiceCatalog().getType().getLabel() + " — " + cp.getServiceCatalog().getDescription())
                    .quantity(1)
                    .unitPrice(cp.getAgreedPrice())
                    .build();

            Invoice invoice = invoiceService.createDirect(
                    cp.getClient().getId(),
                    List.of(line),
                    java.time.LocalDate.now().plusDays(30)
            );
            invoice.setStatus(InvoiceStatus.SENT);
            cp.setInvoice(invoice);
            prestationRepository.save(cp);

            accountingService.createDebitFromPrestation(cp, invoice);
        }
        return unsynced.size();
    }

    @Transactional
    public void delete(Long id) {
        ClientPrestation cp = prestationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignation introuvable : " + id));
        cp.setInvoice(null);
        prestationRepository.delete(cp);
    }
}
