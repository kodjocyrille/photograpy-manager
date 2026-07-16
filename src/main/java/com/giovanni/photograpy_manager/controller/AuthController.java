package com.giovanni.photograpy_manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Email ou mot de passe incorrect.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Vous avez été déconnecté avec succès.");
        }

        return "auth/login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
