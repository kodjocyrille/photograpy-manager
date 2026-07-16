package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.accounting.*;
import com.giovanni.photograpy_manager.domain.billing.PaymentMethod;
import com.giovanni.photograpy_manager.dto.PaymentForm;
import com.giovanni.photograpy_manager.service.AccountingService;
import com.giovanni.photograpy_manager.service.ClientPrestationService;
import com.giovanni.photograpy_manager.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

@Controller
@RequestMapping("/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;
    private final InvoiceService invoiceService;
    private final ClientPrestationService clientPrestationService;

    @GetMapping
    public String dashboard(@RequestParam(required = false, defaultValue = "month") String period, Model model) {
        // Auto-sync: generate invoices for EN_COURS prestations that don't have one yet
        clientPrestationService.syncEnCoursInvoices();

        // --- Period calculation ---
        LocalDate today = LocalDate.now();
        LocalDate from;
        LocalDate to = today;
        String periodLabel;

        switch (period) {
            case "day":
                from = today;
                periodLabel = "Aujourd'hui";
                break;
            case "week":
                from = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                periodLabel = "Cette semaine";
                break;
            case "year":
                from = today.with(TemporalAdjusters.firstDayOfYear());
                periodLabel = "Cette année";
                break;
            case "all":
                from = LocalDate.of(2000, 1, 1);
                periodLabel = "Tout";
                break;
            default: // month
                period = "month";
                from = today.with(TemporalAdjusters.firstDayOfMonth());
                periodLabel = "Ce mois";
                break;
        }

        // --- KPIs with period filter ---
        // Chiffre d'affaires = total payments received (CREDIT entries) in the period
        BigDecimal chiffreAffaires = accountingService.totalCreditsPeriod(from, to);
        // Dépenses = total expenses (DEBIT entries) in the period
        BigDecimal depenses = accountingService.totalDebitsPeriod(from, to);
        // Reste en caisse = CA - Dépenses
        BigDecimal resteEnCaisse = chiffreAffaires.subtract(depenses);
        // Impayés = global (not filtered by period)
        BigDecimal totalUnpaid = invoiceService.totalUnpaid();

        model.addAttribute("chiffreAffaires", chiffreAffaires);
        model.addAttribute("depenses", depenses);
        model.addAttribute("resteEnCaisse", resteEnCaisse);
        model.addAttribute("totalUnpaid", totalUnpaid);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("periodLabel", periodLabel);

        // Chart.js data (always full year)
        int year = Year.now().getValue();
        Map<Integer, Map<String, BigDecimal>> monthlyData = accountingService.monthlyTotals(year);
        StringBuilder credits = new StringBuilder("[");
        StringBuilder debits = new StringBuilder("[");
        for (int m = 1; m <= 12; m++) {
            credits.append(monthlyData.get(m).get("CREDIT"));
            debits.append(monthlyData.get(m).get("DEBIT"));
            if (m < 12) { credits.append(","); debits.append(","); }
        }
        credits.append("]");
        debits.append("]");
        model.addAttribute("chartCredits", credits.toString());
        model.addAttribute("chartDebits", debits.toString());
        model.addAttribute("chartYear", year);

        // Unpaid invoices
        model.addAttribute("unpaidInvoices", invoiceService.findUnpaid());
        model.addAttribute("paymentMethods", PaymentMethod.values());

        model.addAttribute("pageTitle", "Comptabilité");
        return "accounting/dashboard";
    }

    // --- Encaisser un impayé depuis le dashboard ---
    @PostMapping("/unpaid/{invoiceId}/pay")
    public String payUnpaid(@PathVariable Long invoiceId,
                            @Valid @ModelAttribute PaymentForm paymentForm,
                            BindingResult result,
                            RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("errorMessage", "Veuillez remplir correctement le formulaire de paiement");
            return "redirect:/accounting";
        }
        invoiceService.addPayment(invoiceId, paymentForm);
        flash.addFlashAttribute("successMessage", "Paiement encaissé avec succès !");
        return "redirect:/accounting";
    }

    @GetMapping("/journal")
    public String journal(@RequestParam(required = false) EntryType type,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String from,
                          @RequestParam(required = false) String to,
                          Model model) {
        LocalDate dateFrom = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate dateTo = (to != null && !to.isBlank()) ? LocalDate.parse(to) : null;

        model.addAttribute("entries", accountingService.findFiltered(type, categoryId, dateFrom, dateTo));
        model.addAttribute("categories", accountingService.findAllCategories());
        model.addAttribute("types", EntryType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("pageTitle", "Journal comptable");
        return "accounting/journal";
    }

    @GetMapping("/new")
    public String newEntry(Model model) {
        model.addAttribute("categories", accountingService.findActiveCategories());
        model.addAttribute("types", EntryType.values());
        model.addAttribute("pageTitle", "Nouvelle écriture");
        return "accounting/form";
    }

    @PostMapping
    public String create(@RequestParam LocalDate entryDate, @RequestParam EntryType type,
                         @RequestParam String label, @RequestParam BigDecimal amount,
                         @RequestParam(required = false) Long categoryId,
                         RedirectAttributes flash) {
        accountingService.createEntry(entryDate, type, label, amount, categoryId);
        flash.addFlashAttribute("successMessage", "Écriture enregistrée");
        return "redirect:/accounting/journal";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        accountingService.deleteEntry(id);
        flash.addFlashAttribute("successMessage", "Écriture supprimée");
        return "redirect:/accounting/journal";
    }

    // --- Categories ---
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", accountingService.findAllCategories());
        model.addAttribute("categoryTypes", CategoryType.values());
        model.addAttribute("pageTitle", "Catégories comptables");
        return "accounting/categories";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name, @RequestParam CategoryType type,
                                  RedirectAttributes flash) {
        accountingService.createCategory(name, type);
        flash.addFlashAttribute("successMessage", "Catégorie créée");
        return "redirect:/accounting/categories";
    }

    @PostMapping("/categories/{id}/toggle")
    public String toggleCategory(@PathVariable Long id, RedirectAttributes flash) {
        accountingService.toggleCategory(id);
        flash.addFlashAttribute("successMessage", "Statut modifié");
        return "redirect:/accounting/categories";
    }

    // --- CSV Export ---
    @GetMapping("/export")
    public void exportCsv(@RequestParam(required = false) EntryType type,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String from,
                          @RequestParam(required = false) String to,
                          HttpServletResponse response) throws IOException {
        LocalDate dateFrom = (from != null && !from.isBlank()) ? LocalDate.parse(from) : null;
        LocalDate dateTo = (to != null && !to.isBlank()) ? LocalDate.parse(to) : null;

        String csv = accountingService.exportCsv(type, categoryId, dateFrom, dateTo);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=comptabilite_export.csv");
        response.getWriter().write(csv);
        response.getWriter().flush();
    }
}
