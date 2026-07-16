package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.domain.user.User;
import com.giovanni.photograpy_manager.domain.user.UserRole;
import com.giovanni.photograpy_manager.repository.AppointmentRepository;
import com.giovanni.photograpy_manager.repository.UserRepository;
import com.giovanni.photograpy_manager.service.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;
    private final DossierService dossierService;

    // ─── Liste ───────────────────────────────────────────────────────

    @GetMapping
    public String list(@RequestParam(required = false) UserRole filterRole, Model model) {
        List<User> users;
        if (filterRole != null) {
            users = userRepository.findByRoleOrderByFullNameAsc(filterRole);
        } else {
            users = userRepository.findAll();
        }
        model.addAttribute("users", users);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("filterRole", filterRole);
        model.addAttribute("countAll", userRepository.count());
        model.addAttribute("countPhotographers", userRepository.countByRole(UserRole.PHOTOGRAPHER));
        model.addAttribute("countAssistants", userRepository.countByRole(UserRole.ASSISTANT));
        model.addAttribute("countAdmins", userRepository.countByRole(UserRole.ADMIN));
        model.addAttribute("pageTitle", "Équipe");
        return "admin/users";
    }

    // ─── Créer ───────────────────────────────────────────────────────

    @PostMapping
    public String create(@RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam UserRole role,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) List<String> specialties,
                         RedirectAttributes flash) {
        if (userRepository.existsByEmail(email)) {
            flash.addFlashAttribute("errorMessage", "Un utilisateur avec cet email existe déjà");
            return "redirect:/admin/users";
        }

        Set<EventType> specs = new HashSet<>();
        if (specialties != null) {
            specialties.forEach(s -> {
                try { specs.add(EventType.valueOf(s)); } catch (Exception ignored) {}
            });
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .phone(phone)
                .specialties(specs)
                .build();
        userRepository.save(user);
        flash.addFlashAttribute("successMessage", "Employé \"" + fullName + "\" créé avec succès");
        return "redirect:/admin/users";
    }

    // ─── Fiche employé ──────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Utilisateur introuvable"));

        model.addAttribute("user", user);
        model.addAttribute("appointments", appointmentRepository.findByPhotographerIdOrderByStartTimeDesc(id));
        model.addAttribute("dossiers", dossierService.findByPhotographer(id));
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("pageTitle", user.getFullName());
        return "admin/user-detail";
    }

    // ─── Modifier ───────────────────────────────────────────────────

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam UserRole role,
                         @RequestParam(required = false) List<String> specialties,
                         RedirectAttributes flash) {
        User user = userRepository.findById(id).orElseThrow();

        // Vérifier doublon email sauf pour lui-même
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            flash.addFlashAttribute("errorMessage", "Un autre utilisateur utilise déjà cet email");
            return "redirect:/admin/users/" + id;
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);

        Set<EventType> specs = new HashSet<>();
        if (specialties != null) {
            specialties.forEach(s -> {
                try { specs.add(EventType.valueOf(s)); } catch (Exception ignored) {}
            });
        }
        user.setSpecialties(specs);

        userRepository.save(user);
        flash.addFlashAttribute("successMessage", "Profil mis à jour avec succès");
        return "redirect:/admin/users/" + id;
    }

    // ─── Changer mot de passe ───────────────────────────────────────

    @PostMapping("/{id}/password")
    public String changePassword(@PathVariable Long id,
                                 @RequestParam String newPassword,
                                 RedirectAttributes flash) {
        if (newPassword.length() < 6) {
            flash.addFlashAttribute("errorMessage", "Le mot de passe doit contenir au moins 6 caractères");
            return "redirect:/admin/users/" + id;
        }
        User user = userRepository.findById(id).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        flash.addFlashAttribute("successMessage", "Mot de passe mis à jour");
        return "redirect:/admin/users/" + id;
    }

    // ─── Activer / Désactiver ───────────────────────────────────────

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id, RedirectAttributes flash) {
        User user = userRepository.findById(id).orElseThrow();
        user.setActive(!user.isActive());
        userRepository.save(user);
        flash.addFlashAttribute("successMessage",
                user.isActive() ? "Employé activé" : "Employé désactivé");
        return "redirect:/admin/users";
    }

    // ─── Supprimer ──────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        User user = userRepository.findById(id).orElseThrow();
        userRepository.delete(user);
        flash.addFlashAttribute("successMessage", "Employé supprimé");
        return "redirect:/admin/users";
    }
}
