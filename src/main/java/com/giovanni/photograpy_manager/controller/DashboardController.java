package com.giovanni.photograpy_manager.controller;

import com.giovanni.photograpy_manager.domain.billing.ClientPrestation;
import com.giovanni.photograpy_manager.domain.appointment.Appointment;
import com.giovanni.photograpy_manager.domain.user.User;
import com.giovanni.photograpy_manager.repository.UserRepository;
import com.giovanni.photograpy_manager.service.AppointmentService;
import com.giovanni.photograpy_manager.service.ClientPrestationService;
import com.giovanni.photograpy_manager.service.ClientService;
import com.giovanni.photograpy_manager.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final ClientService clientService;
    private final AppointmentService appointmentService;
    private final InvoiceService invoiceService;
    private final ClientPrestationService clientPrestationService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        model.addAttribute("currentUser", user);
        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("clientCount", clientService.count());
        model.addAttribute("rdvCount", appointmentService.countThisMonth());
        model.addAttribute("caThisMonth", invoiceService.revenueThisMonth());

        // Upcoming: RDV et prestations dans les 7 prochains jours
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        List<Appointment> upcomingAppointments = appointmentService.findByDateRange(today, in7Days);
        List<ClientPrestation> upcomingPrestations = clientPrestationService.findByDateRange(today, in7Days);

        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("upcomingPrestations", upcomingPrestations);
        model.addAttribute("prestationCount", clientPrestationService.findAll().size());

        return "dashboard";
    }
}
