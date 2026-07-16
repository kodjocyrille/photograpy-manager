package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.magiclink.MagicLink;
import com.giovanni.photograpy_manager.service.ClientService;
import com.giovanni.photograpy_manager.service.MagicLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/magic-links")
@RequiredArgsConstructor
public class MagicLinkAdminController {

    private final MagicLinkService magicLinkService;
    private final ClientService clientService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("links", magicLinkService.findAll());
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("pageTitle", "Liens magiques");
        return "admin/magiclinks";
    }

    @PostMapping
    public String create(@RequestParam Long clientId,
                         @RequestParam(required = false) String pinCode,
                         @RequestParam(defaultValue = "7") int expirationDays,
                         RedirectAttributes flash) {
        MagicLink link = magicLinkService.create(clientId, null, pinCode, expirationDays);
        flash.addFlashAttribute("successMessage", "Lien magique créé : /gallery/" + link.getToken());
        return "redirect:/admin/magic-links";
    }

    @PostMapping("/{id}/revoke")
    public String revoke(@PathVariable Long id, RedirectAttributes flash) {
        magicLinkService.revoke(id);
        flash.addFlashAttribute("successMessage", "Lien révoqué");
        return "redirect:/admin/magic-links";
    }
}
