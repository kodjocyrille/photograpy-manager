package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.accounting.*;
import com.giovanni.photograpy_manager.domain.billing.Invoice;
import com.giovanni.photograpy_manager.domain.billing.Payment;
import com.giovanni.photograpy_manager.repository.AccountCategoryRepository;
import com.giovanni.photograpy_manager.repository.AccountingEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountingService {

    private final AccountingEntryRepository entryRepository;
    private final AccountCategoryRepository categoryRepository;

    // --- Categories ---

    public List<AccountCategory> findAllCategories() {
        return categoryRepository.findAllByOrderByTypeAscNameAsc();
    }

    public List<AccountCategory> findActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc();
    }

    public List<AccountCategory> findCategoriesByType(CategoryType type) {
        return categoryRepository.findByTypeAndActiveTrueOrderByNameAsc(type);
    }

    @Transactional
    public AccountCategory createCategory(String name, CategoryType type) {
        return categoryRepository.save(AccountCategory.builder()
                .name(name).type(type).build());
    }

    @Transactional
    public void toggleCategory(Long id) {
        AccountCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable"));
        cat.setActive(!cat.isActive());
        categoryRepository.save(cat);
    }

    // --- Entries ---

    public List<AccountingEntry> findAllEntries() {
        return entryRepository.findAllByOrderByEntryDateDescCreatedAtDesc();
    }

    public List<AccountingEntry> findFiltered(EntryType type, Long categoryId, LocalDate from, LocalDate to) {
        return entryRepository.findFiltered(type, categoryId, from, to);
    }

    @Transactional
    public AccountingEntry createEntry(LocalDate date, EntryType type, String label,
                                        BigDecimal amount, Long categoryId) {
        AccountingEntry entry = AccountingEntry.builder()
                .entryDate(date)
                .type(type)
                .label(label)
                .amount(amount)
                .build();
        if (categoryId != null) {
            entry.setCategory(categoryRepository.findById(categoryId).orElse(null));
        }
        return entryRepository.save(entry);
    }

    /**
     * Auto-create CREDIT entry when a payment is recorded on an invoice.
     * Called from InvoiceService.addPayment()
     */
    @Transactional
    public void createCreditFromPayment(Payment payment, Invoice invoice) {
        // Find a default revenue category
        AccountCategory revenueCat = categoryRepository
                .findByTypeAndActiveTrueOrderByNameAsc(CategoryType.REVENUE)
                .stream().findFirst().orElse(null);

        AccountingEntry entry = AccountingEntry.builder()
                .entryDate(payment.getPaymentDate())
                .type(EntryType.CREDIT)
                .label("Paiement " + payment.getMethod().getLabel() + " — " + invoice.getInvoiceNumber())
                .amount(payment.getAmount())
                .invoice(invoice)
                .category(revenueCat)
                .build();
        entryRepository.save(entry);
    }

    /**
     * Auto-create accounting entry (expected revenue / receivable) when a prestation starts.
     * Called from ClientPrestationService.updateStatus() when status → EN_COURS
     */
    @Transactional
    public void createDebitFromPrestation(com.giovanni.photograpy_manager.domain.billing.ClientPrestation prestation, Invoice invoice) {
        AccountCategory revenueCat = categoryRepository
                .findByTypeAndActiveTrueOrderByNameAsc(CategoryType.REVENUE)
                .stream().findFirst().orElse(null);

        AccountingEntry entry = AccountingEntry.builder()
                .entryDate(java.time.LocalDate.now())
                .type(EntryType.CREDIT)
                .label("Impayé — " + prestation.getClient().getFullName() + " — " +
                       prestation.getServiceCatalog().getType().getLabel() + " — " +
                       prestation.getServiceCatalog().getDescription())
                .amount(prestation.getAgreedPrice())
                .invoice(invoice)
                .category(revenueCat)
                .build();
        entryRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long id) {
        entryRepository.deleteById(id);
    }

    // --- Aggregations ---

    public BigDecimal totalCredits() { return entryRepository.totalByType(EntryType.CREDIT); }
    public BigDecimal totalDebits() { return entryRepository.totalByType(EntryType.DEBIT); }
    public BigDecimal balance() { return entryRepository.balance(); }

    public BigDecimal totalCreditsPeriod(LocalDate from, LocalDate to) {
        return entryRepository.totalByTypeAndPeriod(EntryType.CREDIT, from, to);
    }
    public BigDecimal totalDebitsPeriod(LocalDate from, LocalDate to) {
        return entryRepository.totalByTypeAndPeriod(EntryType.DEBIT, from, to);
    }
    public BigDecimal balancePeriod(LocalDate from, LocalDate to) {
        return entryRepository.balanceByPeriod(from, to);
    }

    /**
     * Monthly data for Chart.js — returns map: {month -> {CREDIT: x, DEBIT: y}}
     */
    public Map<Integer, Map<String, BigDecimal>> monthlyTotals(int year) {
        Map<Integer, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, BigDecimal> entry = new HashMap<>();
            entry.put("CREDIT", BigDecimal.ZERO);
            entry.put("DEBIT", BigDecimal.ZERO);
            result.put(m, entry);
        }
        List<Object[]> rows = entryRepository.monthlyTotalsByYear(year);
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            String type = row[1].toString();
            BigDecimal total = (BigDecimal) row[2];
            result.get(month).put(type, total);
        }
        return result;
    }

    /**
     * Export CSV data
     */
    public String exportCsv(EntryType type, Long categoryId, LocalDate from, LocalDate to) {
        List<AccountingEntry> entries = findFiltered(type, categoryId, from, to);
        StringBuilder sb = new StringBuilder();
        sb.append("Date;Type;Libellé;Montant (FCFA);Catégorie\n");
        for (AccountingEntry e : entries) {
            sb.append(e.getEntryDate()).append(";")
              .append(e.getType().getLabel()).append(";")
              .append(e.getLabel()).append(";")
              .append(e.getAmount()).append(";")
              .append(e.getCategory() != null ? e.getCategory().getName() : "").append("\n");
        }
        return sb.toString();
    }
}
