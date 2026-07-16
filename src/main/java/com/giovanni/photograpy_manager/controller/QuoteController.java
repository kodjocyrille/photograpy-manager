package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.billing.*;
import com.giovanni.photograpy_manager.dto.QuoteForm;
import com.giovanni.photograpy_manager.service.CatalogService;
import com.giovanni.photograpy_manager.service.ClientService;
import com.giovanni.photograpy_manager.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/billing/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final ClientService clientService;
    private final CatalogService catalogService;

    @GetMapping
    public String list(@RequestParam(required = false) QuoteStatus status, Model model) {
        model.addAttribute("quotes", status != null
                ? quoteService.findAll().stream().filter(q -> q.getStatus() == status).toList()
                : quoteService.findAll());
        model.addAttribute("statuses", QuoteStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pageTitle", "Devis");
        return "billing/quote/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long clientId, Model model) {
        QuoteForm form = new QuoteForm();
        if (clientId != null) form.setClientId(clientId);
        populateModel(model);
        model.addAttribute("quoteForm", form);
        model.addAttribute("pageTitle", "Nouveau devis");
        return "billing/quote/form";
    }

    @PostMapping
    public String create(@ModelAttribute QuoteForm quoteForm, Model model, RedirectAttributes flash) {
        Quote quote = quoteService.create(quoteForm);
        flash.addFlashAttribute("successMessage", "Devis " + quote.getQuoteNumber() + " créé");
        return "redirect:/billing/quotes/" + quote.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Quote quote = quoteService.findById(id);
        model.addAttribute("quote", quote);
        model.addAttribute("statuses", QuoteStatus.values());
        model.addAttribute("pageTitle", "Devis " + quote.getQuoteNumber());
        return "billing/quote/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Quote quote = quoteService.findById(id);
        QuoteForm form = new QuoteForm();
        form.setId(quote.getId());
        form.setClientId(quote.getClient().getId());
        form.setValidUntil(quote.getValidUntil() != null ? quote.getValidUntil().toString() : "");
        for (QuoteLine ql : quote.getLines()) {
            form.getLines().add(new QuoteForm.LineItem(ql.getDescription(), ql.getQuantity(), ql.getUnitPrice()));
        }
        populateModel(model);
        model.addAttribute("quoteForm", form);
        model.addAttribute("pageTitle", "Modifier — " + quote.getQuoteNumber());
        return "billing/quote/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute QuoteForm quoteForm, RedirectAttributes flash) {
        quoteService.update(id, quoteForm);
        flash.addFlashAttribute("successMessage", "Devis mis à jour");
        return "redirect:/billing/quotes/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam QuoteStatus status, RedirectAttributes flash) {
        quoteService.updateStatus(id, status);
        flash.addFlashAttribute("successMessage", "Statut mis à jour : " + status.getLabel());
        return "redirect:/billing/quotes/" + id;
    }

    private void populateModel(Model model) {
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("catalog", catalogService.findAllActive());
    }
}
