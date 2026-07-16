package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.billing.ClientPrestation;
import com.giovanni.photograpy_manager.domain.billing.Invoice;
import com.giovanni.photograpy_manager.domain.billing.PrestationStatus;
import com.giovanni.photograpy_manager.domain.billing.ServiceCatalog;
import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.service.CatalogService;
import com.giovanni.photograpy_manager.service.ClientPrestationService;
import com.giovanni.photograpy_manager.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final ClientPrestationService clientPrestationService;
    private final ClientService clientService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "catalog") String tab, Model model) {
        model.addAttribute("items", catalogService.findAll());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("assignments", clientPrestationService.findAll());
        model.addAttribute("prestationStatuses", PrestationStatus.values());
        model.addAttribute("activeTab", tab);
        model.addAttribute("pageTitle", "Prestations");
        return "admin/catalog";
    }

    // ─── Catalogue CRUD ───

    @PostMapping
    public String create(@RequestParam EventType type, @RequestParam String description,
                         @RequestParam int minDurationHours, @RequestParam BigDecimal basePrice,
                         RedirectAttributes flash) {
        catalogService.create(type, description, minDurationHours, basePrice);
        flash.addFlashAttribute("successMessage", "Prestation ajoutée au catalogue");
        return "redirect:/admin/catalog?tab=catalog";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam EventType type,
                         @RequestParam String description, @RequestParam int minDurationHours,
                         @RequestParam BigDecimal basePrice, RedirectAttributes flash) {
        catalogService.update(id, type, description, minDurationHours, basePrice);
        flash.addFlashAttribute("successMessage", "Prestation mise à jour");
        return "redirect:/admin/catalog?tab=catalog";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes flash) {
        catalogService.toggleActive(id);
        flash.addFlashAttribute("successMessage", "Statut modifié");
        return "redirect:/admin/catalog?tab=catalog";
    }

    // ─── Assignation prestation → client ───

    @PostMapping("/assign")
    public String assign(@RequestParam Long clientId, @RequestParam Long serviceCatalogId,
                         @RequestParam(required = false) BigDecimal agreedPrice,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledDate,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledEndDate,
                         @RequestParam(required = false) String notes,
                         RedirectAttributes flash) {
        clientPrestationService.assign(clientId, serviceCatalogId, agreedPrice, scheduledDate, scheduledEndDate, notes);
        flash.addFlashAttribute("successMessage", "Prestation assignée au client avec succès");
        return "redirect:/admin/catalog?tab=assignments";
    }

    @PostMapping("/assignments/{id}/status")
    public String updateAssignmentStatus(@PathVariable Long id,
                                         @RequestParam PrestationStatus status,
                                         RedirectAttributes flash) {
        clientPrestationService.updateStatus(id, status);
        flash.addFlashAttribute("successMessage", "Statut de la prestation mis à jour");
        return "redirect:/admin/catalog?tab=assignments";
    }

    @PostMapping("/assignments/{id}/delete")
    public String deleteAssignment(@PathVariable Long id, RedirectAttributes flash) {
        try {
            clientPrestationService.delete(id);
        } catch (RuntimeException e) {
            flash.addFlashAttribute("errorMessage", "Impossible de supprimer cette assignation : " + e.getMessage());
            return "redirect:/admin/catalog?tab=assignments";
        }
        flash.addFlashAttribute("successMessage", "Assignation supprimée");
        return "redirect:/admin/catalog?tab=assignments";
    }

    @PostMapping("/assignments/{id}/invoice")
    public String generateInvoice(@PathVariable Long id, RedirectAttributes flash) {
        try {
            Invoice invoice = clientPrestationService.generateInvoice(id);
            flash.addFlashAttribute("successMessage", "Facture " + invoice.getInvoiceNumber() + " générée avec succès");
        } catch (IllegalStateException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/catalog?tab=assignments";
    }
}
