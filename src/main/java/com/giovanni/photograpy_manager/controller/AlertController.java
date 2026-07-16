package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.alert.AlertType;
import com.giovanni.photograpy_manager.service.AlertService;
import com.giovanni.photograpy_manager.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final EmailService emailService;

    /**
     * Page de configuration des alertes (toggle on/off par type)
     */
    @GetMapping
    public String alertConfig(Model model) {
        model.addAttribute("configs", alertService.findAllConfigs());
        model.addAttribute("totalSent", alertService.countSent());
        model.addAttribute("totalFailed", alertService.countFailed());
        model.addAttribute("pageTitle", "Configuration Alertes");
        return "admin/alerts";
    }

    /**
     * Toggle activation d'un type d'alerte
     */
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes flash) {
        alertService.toggleAlert(id);
        flash.addFlashAttribute("successMessage", "Configuration modifiée");
        return "redirect:/admin/alerts";
    }

    /**
     * Envoi d'un email de test
     */
    @PostMapping("/test")
    public String sendTest(@RequestParam String testEmail, RedirectAttributes flash) {
        emailService.sendEmail(
                AlertType.CLIENT_GALLERY_READY,
                testEmail,
                "Utilisateur Test",
                Map.of("detail", "Ceci est un email de test envoyé depuis l'interface d'administration.",
                        "subject", "Test d'alerte PHOTOAGENCE"));
        flash.addFlashAttribute("successMessage", "Email de test envoyé à " + testEmail);
        return "redirect:/admin/alerts";
    }

    /**
     * Historique des emails envoyés
     */
    @GetMapping("/history")
    public String emailHistory(Model model) {
        model.addAttribute("logs", alertService.findAllLogs());
        model.addAttribute("totalSent", alertService.countSent());
        model.addAttribute("totalFailed", alertService.countFailed());
        model.addAttribute("pageTitle", "Historique des emails");
        return "admin/email-history";
    }
}
