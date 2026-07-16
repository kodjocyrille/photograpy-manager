package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.billing.*;
import com.giovanni.photograpy_manager.dto.PaymentForm;
import com.giovanni.photograpy_manager.service.ClientService;
import com.giovanni.photograpy_manager.service.InvoiceService;
import com.giovanni.photograpy_manager.service.PdfService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/billing/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ClientService clientService;
    private final PdfService pdfService;

    @GetMapping
    public String list(@RequestParam(required = false) InvoiceStatus status, Model model) {
        model.addAttribute("invoices", status != null
                ? invoiceService.findAll().stream().filter(i -> i.getStatus() == status).toList()
                : invoiceService.findAll());
        model.addAttribute("statuses", InvoiceStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pageTitle", "Facturation");
        return "billing/invoice/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.findById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("paymentForm", new PaymentForm());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("statuses", InvoiceStatus.values());
        model.addAttribute("pageTitle", "Facture " + invoice.getInvoiceNumber());
        return "billing/invoice/detail";
    }

    // Créer une facture depuis un devis
    @PostMapping("/from-quote/{quoteId}")
    public String createFromQuote(@PathVariable Long quoteId, RedirectAttributes flash) {
        Invoice invoice = invoiceService.createFromQuote(quoteId);
        flash.addFlashAttribute("successMessage", "Facture " + invoice.getInvoiceNumber() + " créée depuis le devis");
        return "redirect:/billing/invoices/" + invoice.getId();
    }

    // Formulaire facture directe
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long clientId, Model model) {
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("selectedClientId", clientId);
        model.addAttribute("pageTitle", "Nouvelle facture");
        return "billing/invoice/form";
    }

    @PostMapping
    public String createDirect(@RequestParam Long clientId,
                               @RequestParam List<String> lineDescription,
                               @RequestParam List<Integer> lineQuantity,
                               @RequestParam List<BigDecimal> lineUnitPrice,
                               @RequestParam(required = false) String dueDate,
                               RedirectAttributes flash) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (int i = 0; i < lineDescription.size(); i++) {
            if (lineDescription.get(i) == null || lineDescription.get(i).isBlank()) continue;
            lines.add(InvoiceLine.builder()
                    .description(lineDescription.get(i))
                    .quantity(lineQuantity.get(i))
                    .unitPrice(lineUnitPrice.get(i))
                    .build());
        }
        LocalDate due = (dueDate != null && !dueDate.isBlank()) ? LocalDate.parse(dueDate) : null;
        Invoice invoice = invoiceService.createDirect(clientId, lines, due);
        flash.addFlashAttribute("successMessage", "Facture " + invoice.getInvoiceNumber() + " créée");
        return "redirect:/billing/invoices/" + invoice.getId();
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam InvoiceStatus status, RedirectAttributes flash) {
        invoiceService.updateStatus(id, status);
        flash.addFlashAttribute("successMessage", "Statut mis à jour : " + status.getLabel());
        return "redirect:/billing/invoices/" + id;
    }

    @PostMapping("/{id}/payments")
    public String addPayment(@PathVariable Long id,
                             @Valid @ModelAttribute PaymentForm paymentForm,
                             BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            Invoice invoice = invoiceService.findById(id);
            model.addAttribute("invoice", invoice);
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("statuses", InvoiceStatus.values());
            model.addAttribute("pageTitle", "Facture " + invoice.getInvoiceNumber());
            return "billing/invoice/detail";
        }
        invoiceService.addPayment(id, paymentForm);
        flash.addFlashAttribute("successMessage", "Paiement enregistré avec succès");
        return "redirect:/billing/invoices/" + id;
    }

    // --- PDF Download ---

    @GetMapping("/{id}/pdf")
    public void downloadPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Invoice invoice = invoiceService.findById(id);
        byte[] pdf = pdfService.generateInvoicePdf(invoice, false);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=" + invoice.getInvoiceNumber() + ".pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
    }

    @GetMapping("/{id}/receipt")
    public void downloadReceipt(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Invoice invoice = invoiceService.findById(id);
        byte[] pdf = pdfService.generateInvoicePdf(invoice, true);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=RECU-" + invoice.getInvoiceNumber() + ".pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
    }
}
