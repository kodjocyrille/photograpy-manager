package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.magiclink.MagicLink;
import com.giovanni.photograpy_manager.service.MagicLinkService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class GalleryController {

    private final MagicLinkService magicLinkService;

    /**
     * Page galerie publique (pas d'auth requise)
     */
    @GetMapping("/gallery/{token}")
    public String gallery(@PathVariable String token,
                          @RequestParam(required = false) String pin,
                          HttpServletRequest request, Model model) {
        MagicLink link = magicLinkService.findByToken(token);

        if (!link.isAccessible()) {
            model.addAttribute("error", "Ce lien a expiré ou a été révoqué.");
            return "gallery/expired";
        }

        // PIN check
        if (link.getPinCode() != null && !link.getPinCode().isEmpty()) {
            if (pin == null || !pin.equals(link.getPinCode())) {
                model.addAttribute("token", token);
                model.addAttribute("needPin", true);
                if (pin != null) model.addAttribute("pinError", "Code PIN incorrect");
                return "gallery/pin";
            }
        }

        // Log access
        magicLinkService.logAccess(link, request.getRemoteAddr(), "VIEW");

        model.addAttribute("link", link);
        model.addAttribute("clientName", link.getClient().getFullName());
        return "gallery/view";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(Model model) {
        model.addAttribute("error", "Ce lien est invalide ou n'existe pas.");
        return "gallery/expired";
    }
}
