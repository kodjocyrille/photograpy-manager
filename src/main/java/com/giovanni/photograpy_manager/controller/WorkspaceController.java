package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.domain.user.User;
import com.giovanni.photograpy_manager.domain.workspace.*;
import com.giovanni.photograpy_manager.repository.UserRepository;
import com.giovanni.photograpy_manager.service.ClientService;
import com.giovanni.photograpy_manager.service.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final DossierService dossierService;
    private final ClientService clientService;
    private final UserRepository userRepository;

    @GetMapping
    public String kanban(@RequestParam(required = false) DossierStatus status,
                         @RequestParam(required = false) Priority priority,
                         @RequestParam(required = false) Long photographerId,
                         Model model) {
        model.addAttribute("toProcess", dossierService.findByColumn(DossierColumn.TO_PROCESS));
        model.addAttribute("done", dossierService.findByColumn(DossierColumn.DONE));
        model.addAttribute("statuses", DossierStatus.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("pageTitle", "Workspace");
        return "workspace/kanban";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("photographers", userRepository.findAll());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("pageTitle", "Nouveau dossier");
        return "workspace/form";
    }

    @PostMapping
    public String create(@RequestParam Long clientId, @RequestParam EventType serviceType,
                         @RequestParam(required = false) String sessionDate,
                         @RequestParam(required = false) Long photographerId,
                         @RequestParam(required = false) Priority priority,
                         RedirectAttributes flash) {
        LocalDate date = (sessionDate != null && !sessionDate.isBlank()) ? LocalDate.parse(sessionDate) : null;
        dossierService.create(clientId, serviceType, date, photographerId, priority);
        flash.addFlashAttribute("successMessage", "Dossier créé avec succès");
        return "redirect:/workspace";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Dossier dossier = dossierService.findById(id);
        model.addAttribute("dossier", dossier);
        model.addAttribute("statuses", DossierStatus.values());
        model.addAttribute("pageTitle", "Dossier " + dossier.getDossierCode());
        return "workspace/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam DossierStatus status,
                               Authentication auth, RedirectAttributes flash) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        dossierService.updateStatus(id, status, user);
        flash.addFlashAttribute("successMessage", "Statut mis à jour : " + status.getLabel());
        return "redirect:/workspace/" + id;
    }

    @PostMapping("/{id}/column")
    public String moveColumn(@PathVariable Long id, @RequestParam DossierColumn column,
                             RedirectAttributes flash) {
        dossierService.moveColumn(id, column);
        flash.addFlashAttribute("successMessage", "Dossier déplacé");
        return "redirect:/workspace";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id, @RequestParam String content,
                             Authentication auth, RedirectAttributes flash) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        dossierService.addComment(id, user, content);
        flash.addFlashAttribute("successMessage", "Commentaire ajouté");
        return "redirect:/workspace/" + id;
    }
}
