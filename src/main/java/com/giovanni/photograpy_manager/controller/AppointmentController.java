package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.appointment.Appointment;
import com.giovanni.photograpy_manager.domain.appointment.AppointmentStatus;
import com.giovanni.photograpy_manager.domain.appointment.AppointmentType;
import com.giovanni.photograpy_manager.domain.user.UserRole;
import com.giovanni.photograpy_manager.dto.AppointmentForm;
import com.giovanni.photograpy_manager.domain.billing.PrestationStatus;
import com.giovanni.photograpy_manager.service.AppointmentService;
import com.giovanni.photograpy_manager.service.ClientPrestationService;
import com.giovanni.photograpy_manager.service.ClientService;
import com.giovanni.photograpy_manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ClientService clientService;
    private final UserRepository userRepository;
    private final ClientPrestationService clientPrestationService;

    @GetMapping
    public String calendar(@RequestParam(required = false) String start,
                           @RequestParam(required = false) String end,
                           Model model) {
        LocalDate startDate = start != null ? LocalDate.parse(start) : LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = end != null ? LocalDate.parse(end) : startDate.plusMonths(1);

        List<Appointment> appointments = appointmentService.findByDateRange(startDate, endDate);
        model.addAttribute("appointments", appointments);
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("pageTitle", "Agenda");
        return "appointment/calendar";
    }

    // Endpoint JSON pour FullCalendar — RDV + Prestations
    @GetMapping("/events")
    @ResponseBody
    public List<java.util.Map<String, Object>> events(@RequestParam String start, @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start.substring(0, 10));
        LocalDate endDate = LocalDate.parse(end.substring(0, 10));

        List<java.util.Map<String, Object>> allEvents = new java.util.ArrayList<>();

        // 1. Rendez-vous
        appointmentService.findByDateRange(startDate, endDate).forEach(a -> {
            java.util.Map<String, Object> ev = new java.util.HashMap<>();
            ev.put("id", a.getId());
            ev.put("title", a.getClient().getFullName() + " — " + a.getType().getLabel());
            ev.put("start", a.getStartTime().toString());
            ev.put("end", a.getEndTime().toString());
            ev.put("color", getStatusColor(a.getStatus()));
            ev.put("extendedProps", java.util.Map.of(
                    "eventType", "appointment",
                    "status", a.getStatus().name(),
                    "clientId", a.getClient().getId(),
                    "location", a.getLocation() != null ? a.getLocation() : ""
            ));
            allEvents.add(ev);
        });

        // 2. Prestations planifiées
        clientPrestationService.findByDateRange(startDate, endDate).forEach(cp -> {
            java.util.Map<String, Object> ev = new java.util.HashMap<>();
            ev.put("id", "prestation-" + cp.getId());
            ev.put("title", "📸 " + cp.getClient().getFullName() + " — " + cp.getServiceCatalog().getType().getLabel());
            ev.put("start", cp.getScheduledDate().toString());
            ev.put("end", cp.getScheduledEndDate() != null ? cp.getScheduledEndDate().toString() : cp.getScheduledDate().plusHours(2).toString());
            ev.put("title", cp.getClient().getFullName() + " - " + cp.getServiceCatalog().getType().getLabel());
            ev.remove("end");
            ev.put("allDay", true);
            ev.put("display", "list-item");
            ev.put("color", getPrestationColor(cp.getStatus()));
            java.util.Map<String, Object> props = new java.util.HashMap<>();
            props.put("eventType", "prestation");
            props.put("status", cp.getStatus().name());
            props.put("statusLabel", cp.getStatus().getLabel());
            props.put("clientId", cp.getClient().getId());
            props.put("clientName", cp.getClient().getFullName());
            props.put("prestationId", cp.getId());
            props.put("type", cp.getServiceCatalog().getType().getLabel());
            props.put("description", cp.getServiceCatalog().getDescription());
            props.put("agreedPrice", cp.getAgreedPrice().toPlainString());
            props.put("scheduledDate", cp.getScheduledDate().toString());
            props.put("scheduledEndDate", cp.getScheduledEndDate() != null ? cp.getScheduledEndDate().toString() : "");
            props.put("notes", cp.getNotes() != null ? cp.getNotes() : "");
            ev.put("extendedProps", props);
            allEvents.add(ev);
        });

        return allEvents;
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long clientId, Model model) {
        AppointmentForm form = new AppointmentForm();
        if (clientId != null) form.setClientId(clientId);
        populateFormModel(model);
        model.addAttribute("appointmentForm", form);
        model.addAttribute("pageTitle", "Nouveau rendez-vous");
        return "appointment/form";
    }

    @PostMapping
    public Object create(@Valid @ModelAttribute AppointmentForm appointmentForm,
                         BindingResult result, Model model, RedirectAttributes flash,
                         HttpServletResponse response,
                         @RequestHeader(value = "X-Requested-With", required = false) String xRequestedWith) {
        boolean isAjax = "XMLHttpRequest".equals(xRequestedWith);

        // Conflict check
        if (appointmentForm.getPhotographerId() != null &&
            appointmentService.hasConflict(appointmentForm.getPhotographerId(),
                    appointmentForm.getStartTime(), appointmentForm.getEndTime(), null)) {
            if (isAjax) {
                return ResponseEntity.unprocessableEntity()
                        .body(Map.of("success", false, "message", "Conflit d'agenda détecté pour ce photographe"));
            }
            result.rejectValue("startTime", "conflict", "Conflit d'agenda détecté pour ce photographe");
        }

        // Validation errors
        if (result.hasErrors()) {
            if (isAjax) {
                String firstError = result.getFieldErrors().stream()
                        .map(e -> e.getDefaultMessage())
                        .findFirst().orElse("Veuillez corriger les erreurs du formulaire");
                return ResponseEntity.unprocessableEntity()
                        .body(Map.of("success", false, "message", firstError));
            }
            response.setStatus(HttpServletResponse.SC_UNPROCESSABLE_CONTENT);
            populateFormModel(model);
            model.addAttribute("pageTitle", "Nouveau rendez-vous");
            return "appointment/form";
        }

        try {
            Appointment apt = appointmentService.create(appointmentForm);
            if (isAjax) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Rendez-vous créé avec succès",
                        "redirectUrl", "/appointments/" + apt.getId()
                ));
            }
            flash.addFlashAttribute("successMessage", "Rendez-vous créé avec succès");
            return "redirect:/appointments/" + apt.getId();
        } catch (Exception e) {
            log.error("Erreur lors de la création du RDV", e);
            if (isAjax) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("success", false, "message", "Erreur serveur : " + e.getMessage()));
            }
            model.addAttribute("errorMessage", "Erreur : " + e.getMessage());
            populateFormModel(model);
            model.addAttribute("pageTitle", "Nouveau rendez-vous");
            return "appointment/form";
        }
    }

    @GetMapping("/{id}")
    public Object detail(@PathVariable Long id, Model model,
                         @RequestHeader(value = "X-Requested-With", required = false) String xRequestedWith) {
        Appointment apt = appointmentService.findById(id);

        if ("XMLHttpRequest".equals(xRequestedWith)) {
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("id", apt.getId());
            data.put("clientName", apt.getClient().getFullName());
            data.put("clientId", apt.getClient().getId());
            data.put("clientEmail", apt.getClient().getEmail());
            data.put("clientPhone", apt.getClient().getPhone());
            data.put("type", apt.getType().getLabel());
            data.put("startTime", apt.getStartTime().toString());
            data.put("endTime", apt.getEndTime().toString());
            data.put("location", apt.getLocation());
            data.put("photographer", apt.getPhotographer() != null ? apt.getPhotographer().getFullName() : null);
            data.put("status", apt.getStatus().name());
            data.put("statusLabel", apt.getStatus().getLabel());
            data.put("notes", apt.getNotes());
            data.put("createdAt", apt.getCreatedAt().toString());
            return ResponseEntity.ok(data);
        }

        model.addAttribute("appointment", apt);
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("pageTitle", "RDV — " + apt.getClient().getFullName());
        return "appointment/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Appointment apt = appointmentService.findById(id);
        AppointmentForm form = new AppointmentForm();
        form.setId(apt.getId());
        form.setClientId(apt.getClient().getId());
        form.setType(apt.getType());
        form.setStartTime(apt.getStartTime());
        form.setEndTime(apt.getEndTime());
        form.setLocation(apt.getLocation());
        form.setPhotographerId(apt.getPhotographer() != null ? apt.getPhotographer().getId() : null);
        form.setNotes(apt.getNotes());
        populateFormModel(model);
        model.addAttribute("appointmentForm", form);
        model.addAttribute("pageTitle", "Modifier RDV");
        return "appointment/form";
    }

    @PostMapping("/{id}")
    public Object update(@PathVariable Long id,
                         @Valid @ModelAttribute AppointmentForm appointmentForm,
                         BindingResult result, Model model, RedirectAttributes flash,
                         HttpServletResponse response,
                         @RequestHeader(value = "X-Requested-With", required = false) String xRequestedWith) {
        boolean isAjax = "XMLHttpRequest".equals(xRequestedWith);

        if (result.hasErrors()) {
            if (isAjax) {
                String firstError = result.getFieldErrors().stream()
                        .map(e -> e.getDefaultMessage())
                        .findFirst().orElse("Veuillez corriger les erreurs du formulaire");
                return ResponseEntity.unprocessableEntity()
                        .body(Map.of("success", false, "message", firstError));
            }
            response.setStatus(HttpServletResponse.SC_UNPROCESSABLE_CONTENT);
            populateFormModel(model);
            model.addAttribute("pageTitle", "Modifier RDV");
            return "appointment/form";
        }
        try {
            appointmentService.update(id, appointmentForm);
            if (isAjax) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Rendez-vous mis à jour",
                        "redirectUrl", "/appointments/" + id
                ));
            }
            flash.addFlashAttribute("successMessage", "Rendez-vous mis à jour");
            return "redirect:/appointments/" + id;
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du RDV {}", id, e);
            if (isAjax) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("success", false, "message", "Erreur serveur : " + e.getMessage()));
            }
            model.addAttribute("errorMessage", "Erreur : " + e.getMessage());
            populateFormModel(model);
            model.addAttribute("pageTitle", "Modifier RDV");
            return "appointment/form";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam AppointmentStatus status,
                               RedirectAttributes flash) {
        appointmentService.updateStatus(id, status);
        flash.addFlashAttribute("successMessage", "Statut mis à jour : " + status.getLabel());
        return "redirect:/appointments/" + id;
    }

    private void populateFormModel(Model model) {
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("photographers", userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.PHOTOGRAPHER || u.getRole() == UserRole.ADMIN)
                .collect(Collectors.toList()));
        model.addAttribute("appointmentTypes", AppointmentType.values());
    }

    private String getStatusColor(AppointmentStatus status) {
        return switch (status) {
            case CONFIRMED -> "#22c55e";
            case PENDING -> "#f59e0b";
            case CANCELLED -> "#ef4444";
            case POSTPONED -> "#8b5cf6";
            case DONE -> "#6b7280";
        };
    }

    private String getPrestationColor(PrestationStatus status) {
        return switch (status) {
            case EN_ATTENTE -> "#3b82f6";  // bleu
            case EN_COURS -> "#0ea5e9";    // bleu clair
            case TERMINEE -> "#6b7280";    // gris
            case ANNULEE -> "#ef4444";     // rouge
        };
    }
}
