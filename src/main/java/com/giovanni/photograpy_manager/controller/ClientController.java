package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.client.Client;
import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.dto.ClientForm;
import com.giovanni.photograpy_manager.service.AppointmentService;
import com.giovanni.photograpy_manager.service.ClientService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final AppointmentService appointmentService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) EventType type,
                       Model model) {
        if (type != null) {
            model.addAttribute("clients", clientService.findByServiceType(type));
        } else {
            model.addAttribute("clients", clientService.search(search));
        }
        model.addAttribute("search", search);
        model.addAttribute("selectedType", type);
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("pageTitle", "Clients");
        return "client/list";
    }

    // Fragment pour recherche live (Alpine.js fetch)
    @GetMapping("/search-fragment")
    public String searchFragment(@RequestParam String q, Model model) {
        model.addAttribute("clients", clientService.search(q));
        return "client/list :: clientTable";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("clientForm", new ClientForm());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("pageTitle", "Nouveau client");
        return "client/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ClientForm clientForm,
                         BindingResult result, Model model,
                         RedirectAttributes flash, HttpServletResponse response) {
        if (clientService.existsByEmail(clientForm.getEmail())) {
            result.rejectValue("email", "duplicate", "Un client avec cet email existe déjà");
        }
        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_UNPROCESSABLE_CONTENT);
            model.addAttribute("eventTypes", EventType.values());
            model.addAttribute("pageTitle", "Nouveau client");
            return "client/form";
        }
        Client client = clientService.create(clientForm);
        flash.addFlashAttribute("successMessage", "Client " + client.getFullName() + " créé avec succès");
        return "redirect:/clients/" + client.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Client client = clientService.findById(id);
        model.addAttribute("client", client);
        model.addAttribute("appointments", appointmentService.findByClient(id));
        model.addAttribute("pageTitle", client.getFullName());
        return "client/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Client client = clientService.findById(id);
        ClientForm form = new ClientForm();
        form.setId(client.getId());
        form.setFullName(client.getFullName());
        form.setPhone(client.getPhone());
        form.setEmail(client.getEmail());
        form.setServiceType(client.getServiceType());
        form.setEventDate(client.getEventDate());
        model.addAttribute("clientForm", form);
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("pageTitle", "Modifier — " + client.getFullName());
        return "client/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ClientForm clientForm,
                         BindingResult result, Model model,
                         RedirectAttributes flash, HttpServletResponse response) {
        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_UNPROCESSABLE_CONTENT);
            model.addAttribute("eventTypes", EventType.values());
            model.addAttribute("pageTitle", "Modifier client");
            return "client/form";
        }
        clientService.update(id, clientForm);
        flash.addFlashAttribute("successMessage", "Client mis à jour avec succès");
        return "redirect:/clients/" + id;
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes flash) {
        clientService.archive(id);
        flash.addFlashAttribute("successMessage", "Client archivé");
        return "redirect:/clients";
    }

    // Fragment pour détection doublon email (Alpine.js fetch)
    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<String> checkEmail(@RequestParam String email,
                                              @RequestParam(required = false) Long excludeId) {
        boolean exists = clientService.existsByEmail(email);
        if (exists && excludeId != null) {
            Client existing = clientService.findById(excludeId);
            if (existing.getEmail().equalsIgnoreCase(email)) exists = false;
        }
        return ResponseEntity.ok(exists ? "exists" : "available");
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = clientService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clients.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.getBytes());
    }
}
