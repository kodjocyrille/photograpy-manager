package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.billing.*;
import com.giovanni.photograpy_manager.dto.QuoteForm;
import com.giovanni.photograpy_manager.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;

    public List<Quote> findAll() {
        return quoteRepository.findAllByOrderByCreatedAtDesc();
    }

    public Quote findById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Devis introuvable : " + id));
    }

    public List<Quote> findByClient(Long clientId) {
        return quoteRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Transactional
    public Quote create(QuoteForm form) {
        Quote quote = Quote.builder()
                .quoteNumber(generateNumber())
                .client(clientRepository.findById(form.getClientId())
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable")))
                .validUntil(form.getValidUntil() != null && !form.getValidUntil().isBlank()
                        ? LocalDate.parse(form.getValidUntil()) : LocalDate.now().plusDays(30))
                .build();

        for (QuoteForm.LineItem li : form.getLines()) {
            if (li.getDescription() == null || li.getDescription().isBlank()) continue;
            QuoteLine line = QuoteLine.builder()
                    .description(li.getDescription())
                    .quantity(li.getQuantity() > 0 ? li.getQuantity() : 1)
                    .unitPrice(li.getUnitPrice() != null ? li.getUnitPrice() : BigDecimal.ZERO)
                    .build();
            quote.addLine(line);
        }

        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote update(Long id, QuoteForm form) {
        Quote quote = findById(id);
        quote.setClient(clientRepository.findById(form.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable")));
        quote.setValidUntil(form.getValidUntil() != null && !form.getValidUntil().isBlank()
                ? LocalDate.parse(form.getValidUntil()) : quote.getValidUntil());

        // Replace lines
        quote.getLines().clear();
        for (QuoteForm.LineItem li : form.getLines()) {
            if (li.getDescription() == null || li.getDescription().isBlank()) continue;
            QuoteLine line = QuoteLine.builder()
                    .description(li.getDescription())
                    .quantity(li.getQuantity() > 0 ? li.getQuantity() : 1)
                    .unitPrice(li.getUnitPrice() != null ? li.getUnitPrice() : BigDecimal.ZERO)
                    .build();
            quote.addLine(line);
        }

        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote updateStatus(Long id, QuoteStatus status) {
        Quote quote = findById(id);
        quote.setStatus(status);
        return quoteRepository.save(quote);
    }

    private String generateNumber() {
        long count = quoteRepository.count() + 1;
        return String.format("DEV-%d-%04d", Year.now().getValue(), count);
    }
}
