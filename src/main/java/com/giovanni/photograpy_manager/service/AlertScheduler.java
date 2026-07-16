package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.alert.AlertType;
import com.giovanni.photograpy_manager.domain.appointment.Appointment;
import com.giovanni.photograpy_manager.domain.billing.ClientPrestation;
import com.giovanni.photograpy_manager.domain.billing.Invoice;
import com.giovanni.photograpy_manager.domain.billing.InvoiceStatus;
import com.giovanni.photograpy_manager.event.AlertEvent;
import com.giovanni.photograpy_manager.repository.AppointmentRepository;
import com.giovanni.photograpy_manager.repository.ClientPrestationRepository;
import com.giovanni.photograpy_manager.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Scheduler pour les rappels automatiques :
 * - J-2, J-1 : rappels RDV client
 * - J-2, J-1 : rappels prestation client
 * - J-1 : rappel interne (RDV/prestation demain)
 * - J+5, J+15 : relances factures impayées
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AppointmentRepository appointmentRepository;
    private final ClientPrestationRepository prestationRepository;
    private final InvoiceRepository invoiceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AlertService alertService;

    /**
     * Tous les jours à 8h : rappels RDV
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendAppointmentReminders() {
        LocalDate today = LocalDate.now();
        LocalDate in2Days = today.plusDays(2);
        LocalDate tomorrow = today.plusDays(1);

        // J-2 client reminders
        if (alertService.isEnabled(AlertType.CLIENT_APPOINTMENT_REMINDER_J2)) {
            List<Appointment> j2Appointments = appointmentRepository.findByDateRange(
                    in2Days.atStartOfDay(), in2Days.atTime(23, 59));
            for (Appointment apt : j2Appointments) {
                if (apt.getClient().getEmail() != null) {
                    eventPublisher.publishEvent(new AlertEvent(this,
                            AlertType.CLIENT_APPOINTMENT_REMINDER_J2,
                            apt.getClient().getEmail(),
                            apt.getClient().getFullName(),
                            Map.of("appointmentDate", apt.getStartTime().toString(),
                                    "detail", "Rendez-vous le " + in2Days)));
                }
            }
            log.info("📅 {} rappels J-2 traités", j2Appointments.size());
        }

        // J-1 client reminders
        if (alertService.isEnabled(AlertType.CLIENT_APPOINTMENT_REMINDER_J1)) {
            List<Appointment> j1Appointments = appointmentRepository.findByDateRange(
                    tomorrow.atStartOfDay(), tomorrow.atTime(23, 59));
            for (Appointment apt : j1Appointments) {
                if (apt.getClient().getEmail() != null) {
                    eventPublisher.publishEvent(new AlertEvent(this,
                            AlertType.CLIENT_APPOINTMENT_REMINDER_J1,
                            apt.getClient().getEmail(),
                            apt.getClient().getFullName(),
                            Map.of("appointmentDate", apt.getStartTime().toString(),
                                    "detail", "Rendez-vous demain !")));
                }
            }
            log.info("📅 {} rappels J-1 traités", j1Appointments.size());
        }

        // Internal reminder: appointments tomorrow
        if (alertService.isEnabled(AlertType.INTERNAL_APPOINTMENT_TOMORROW)) {
            List<Appointment> tomorrowApts = appointmentRepository.findByDateRange(
                    tomorrow.atStartOfDay(), tomorrow.atTime(23, 59));
            if (!tomorrowApts.isEmpty()) {
                eventPublisher.publishEvent(new AlertEvent(this,
                        AlertType.INTERNAL_APPOINTMENT_TOMORROW,
                        "admin@photoagence.com",
                        "Équipe",
                        Map.of("count", tomorrowApts.size(),
                                "detail", tomorrowApts.size() + " RDV programmé(s) demain")));
            }
        }
    }

    /**
     * Tous les jours à 8h : rappels prestations
     */
    @Scheduled(cron = "0 5 8 * * *")
    public void sendPrestationReminders() {
        LocalDate today = LocalDate.now();
        LocalDate in2Days = today.plusDays(2);
        LocalDate tomorrow = today.plusDays(1);

        // J-2 client prestation reminders
        if (alertService.isEnabled(AlertType.CLIENT_PRESTATION_REMINDER_J2)) {
            List<ClientPrestation> j2Prestations = prestationRepository.findByScheduledDate(in2Days);
            for (ClientPrestation cp : j2Prestations) {
                if (cp.getClient().getEmail() != null) {
                    eventPublisher.publishEvent(new AlertEvent(this,
                            AlertType.CLIENT_PRESTATION_REMINDER_J2,
                            cp.getClient().getEmail(),
                            cp.getClient().getFullName(),
                            Map.of("prestationType", cp.getServiceCatalog().getType().getLabel(),
                                    "detail", "Prestation prévue le " + in2Days + " : " + cp.getServiceCatalog().getDescription())));
                }
            }
            log.info("📸 {} rappels prestation J-2 traités", j2Prestations.size());
        }

        // J-1 client prestation reminders
        if (alertService.isEnabled(AlertType.CLIENT_PRESTATION_REMINDER_J1)) {
            List<ClientPrestation> j1Prestations = prestationRepository.findByScheduledDate(tomorrow);
            for (ClientPrestation cp : j1Prestations) {
                if (cp.getClient().getEmail() != null) {
                    eventPublisher.publishEvent(new AlertEvent(this,
                            AlertType.CLIENT_PRESTATION_REMINDER_J1,
                            cp.getClient().getEmail(),
                            cp.getClient().getFullName(),
                            Map.of("prestationType", cp.getServiceCatalog().getType().getLabel(),
                                    "detail", "Prestation demain : " + cp.getServiceCatalog().getDescription())));
                }
            }
            log.info("📸 {} rappels prestation J-1 traités", j1Prestations.size());
        }

        // Internal reminder: prestations tomorrow
        if (alertService.isEnabled(AlertType.INTERNAL_PRESTATION_TOMORROW)) {
            List<ClientPrestation> tomorrowPrestations = prestationRepository.findByScheduledDate(tomorrow);
            if (!tomorrowPrestations.isEmpty()) {
                eventPublisher.publishEvent(new AlertEvent(this,
                        AlertType.INTERNAL_PRESTATION_TOMORROW,
                        "admin@photoagence.com",
                        "Équipe",
                        Map.of("count", tomorrowPrestations.size(),
                                "detail", tomorrowPrestations.size() + " prestation(s) prévue(s) demain")));
            }
        }
    }

    /**
     * Tous les jours à 9h : relances factures impayées
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendOverdueInvoiceAlerts() {
        LocalDate today = LocalDate.now();

        List<Invoice> allInvoices = invoiceRepository.findAll();
        for (Invoice inv : allInvoices) {
            if (inv.getStatus() == InvoiceStatus.PAID || inv.getDueDate() == null) continue;

            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(inv.getDueDate(), today);

            // Client overdue (J+7)
            if (daysOverdue == 7 && alertService.isEnabled(AlertType.CLIENT_INVOICE_OVERDUE)) {
                eventPublisher.publishEvent(new AlertEvent(this,
                        AlertType.CLIENT_INVOICE_OVERDUE,
                        inv.getClient().getEmail(),
                        inv.getClient().getFullName(),
                        Map.of("invoiceNumber", inv.getInvoiceNumber(),
                                "amount", inv.getTotalAmount(),
                                "detail", "Facture " + inv.getInvoiceNumber() + " en retard de 7 jours")));
            }

            // Internal J+5
            if (daysOverdue == 5 && alertService.isEnabled(AlertType.INTERNAL_INVOICE_OVERDUE_J5)) {
                eventPublisher.publishEvent(new AlertEvent(this,
                        AlertType.INTERNAL_INVOICE_OVERDUE_J5,
                        "admin@photoagence.com",
                        "Administration",
                        Map.of("invoiceNumber", inv.getInvoiceNumber(),
                                "clientName", inv.getClient().getFullName(),
                                "detail", "Facture " + inv.getInvoiceNumber() + " impayée depuis 5 jours")));
            }

            // Internal J+15
            if (daysOverdue == 15 && alertService.isEnabled(AlertType.INTERNAL_INVOICE_OVERDUE_J15)) {
                eventPublisher.publishEvent(new AlertEvent(this,
                        AlertType.INTERNAL_INVOICE_OVERDUE_J15,
                        "admin@photoagence.com",
                        "Administration",
                        Map.of("invoiceNumber", inv.getInvoiceNumber(),
                                "clientName", inv.getClient().getFullName(),
                                "detail", "🚨 Facture " + inv.getInvoiceNumber() + " impayée depuis 15 jours !")));
            }
        }
    }
}
