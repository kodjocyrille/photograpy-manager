package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.billing.*;
import com.giovanni.photograpy_manager.dto.PaymentForm;
import com.giovanni.photograpy_manager.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;
    private final QuoteRepository quoteRepository;
    private final AccountingService accountingService;

    public List<Invoice> findAll() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture introuvable : " + id));
    }

    public List<Invoice> findByClient(Long clientId) {
        return invoiceRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    /**
     * Transforme un devis accepté en facture
     */
    @Transactional
    public Invoice createFromQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Devis introuvable"));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateNumber())
                .client(quote.getClient())
                .quote(quote)
                .dueDate(java.time.LocalDate.now().plusDays(30))
                .build();

        for (QuoteLine ql : quote.getLines()) {
            InvoiceLine il = InvoiceLine.builder()
                    .description(ql.getDescription())
                    .quantity(ql.getQuantity())
                    .unitPrice(ql.getUnitPrice())
                    .build();
            invoice.addLine(il);
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quoteRepository.save(quote);

        return invoiceRepository.save(invoice);
    }

    /**
     * Crée une facture directement (sans devis)
     */
    @Transactional
    public Invoice createDirect(Long clientId, List<InvoiceLine> lines, java.time.LocalDate dueDate) {
        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateNumber())
                .client(clientRepository.findById(clientId)
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable")))
                .dueDate(dueDate != null ? dueDate : java.time.LocalDate.now().plusDays(30))
                .build();

        for (InvoiceLine line : lines) {
            invoice.addLine(line);
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice updateStatus(Long id, InvoiceStatus status) {
        Invoice invoice = findById(id);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    /**
     * Enregistre un paiement partiel ou total
     */
    @Transactional
    public Payment addPayment(Long invoiceId, PaymentForm form) {
        Invoice invoice = findById(invoiceId);

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(form.getAmount())
                .method(form.getMethod())
                .paymentDate(form.getPaymentDate())
                .build();

        paymentRepository.save(payment);

        // Recalculate
        invoice.getPayments().add(payment);
        invoice.recalculateAmountPaid();
        invoiceRepository.save(invoice);

        // Auto-create CREDIT accounting entry
        accountingService.createCreditFromPayment(payment, invoice);

        return payment;
    }

    public BigDecimal revenueThisMonth() {
        YearMonth ym = YearMonth.now();
        return invoiceRepository.revenueByMonth(ym.getMonthValue(), ym.getYear());
    }

    public List<Invoice> findUnpaid() {
        return invoiceRepository.findUnpaidInvoices();
    }

    public BigDecimal totalUnpaid() {
        return invoiceRepository.totalUnpaidAmount();
    }

    private String generateNumber() {
        long count = invoiceRepository.count() + 1;
        return String.format("FAC-%d-%04d", Year.now().getValue(), count);
    }
}
