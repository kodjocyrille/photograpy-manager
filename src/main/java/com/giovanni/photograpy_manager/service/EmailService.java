package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.alert.*;
import com.giovanni.photograpy_manager.event.AlertEvent;
import com.giovanni.photograpy_manager.repository.AlertConfigRepository;
import com.giovanni.photograpy_manager.repository.EmailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Service d'envoi d'emails avec templates Thymeleaf.
 * Écoute les AlertEvents et envoie les emails si le type est activé.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AlertConfigRepository alertConfigRepository;
    private final EmailLogRepository emailLogRepository;

    @EventListener
    @Async
    public void handleAlertEvent(AlertEvent event) {
        AlertType type = event.getAlertType();

        // Check if this alert type is enabled
        AlertConfig config = alertConfigRepository.findByAlertType(type).orElse(null);
        if (config != null && !config.isEnabled()) {
            log.debug("Alert {} is disabled, skipping email to {}", type, event.getRecipientEmail());
            return;
        }

        sendEmail(type, event.getRecipientEmail(), event.getRecipientName(), event.getData());
    }

    /**
     * Envoie un email via template Thymeleaf
     */
    public void sendEmail(AlertType type, String toEmail, String toName, Map<String, Object> data) {
        String subject = resolveSubject(type, data);
        String body;

        // Build template context
        Context ctx = new Context();
        ctx.setVariable("recipientName", toName);
        ctx.setVariable("alertType", type);
        if (data != null) data.forEach(ctx::setVariable);

        try {
            body = templateEngine.process("email/" + type.name().toLowerCase(), ctx);
        } catch (Exception e) {
            // Fallback to generic template
            log.warn("Template email/{} not found, using generic", type.name().toLowerCase());
            ctx.setVariable("subject", subject);
            ctx.setVariable("message", resolveMessage(type, data));
            body = templateEngine.process("email/generic", ctx);
        }

        EmailLog emailLog = EmailLog.builder()
                .alertType(type)
                .recipientEmail(toEmail)
                .recipientName(toName)
                .subject(subject)
                .bodyPreview(body.length() > 500 ? body.substring(0, 500) : body)
                .status(EmailStatus.PENDING)
                .build();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@photoagence.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            emailLog.setStatus(EmailStatus.SENT);
            log.info("✉️ Email [{}] envoyé à {}", type.getLabel(), toEmail);
        } catch (MessagingException e) {
            emailLog.setStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("❌ Échec envoi email [{}] à {} : {}", type.getLabel(), toEmail, e.getMessage());
        } catch (Exception e) {
            emailLog.setStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("❌ Erreur inattendue envoi email : {}", e.getMessage());
        }

        emailLogRepository.save(emailLog);
    }

    private String resolveSubject(AlertType type, Map<String, Object> data) {
        return switch (type) {
            case CLIENT_APPOINTMENT_CONFIRMATION -> "📅 Confirmation de votre rendez-vous — PHOTOAGENCE";
            case CLIENT_APPOINTMENT_REMINDER_J2 -> "📅 Rappel : votre rendez-vous dans 2 jours";
            case CLIENT_APPOINTMENT_REMINDER_J1 -> "📅 Rappel : votre rendez-vous est demain !";
            case CLIENT_QUOTE_SENT -> "📋 Votre devis PHOTOAGENCE";
            case CLIENT_INVOICE_SENT -> "📄 Votre facture PHOTOAGENCE";
            case CLIENT_PAYMENT_RECEIVED -> "✅ Paiement reçu — PHOTOAGENCE";
            case CLIENT_GALLERY_READY -> "📸 Votre galerie photo est disponible !";
            case CLIENT_INVOICE_OVERDUE -> "⚠️ Facture en attente de paiement";
            case CLIENT_PRESTATION_REMINDER_J2 -> "📸 Rappel : votre prestation dans 2 jours";
            case CLIENT_PRESTATION_REMINDER_J1 -> "📸 Rappel : votre prestation est demain !";
            case INTERNAL_NEW_APPOINTMENT -> "🆕 Nouveau RDV planifié";
            case INTERNAL_APPOINTMENT_TOMORROW -> "📅 Rappel : RDV demain";
            case INTERNAL_PRESTATION_TOMORROW -> "📸 Rappel : prestation demain";
            case INTERNAL_INVOICE_OVERDUE_J5 -> "⚠️ Facture impayée depuis 5 jours";
            case INTERNAL_INVOICE_OVERDUE_J15 -> "🚨 Facture impayée depuis 15 jours";
            case INTERNAL_DOSSIER_CREATED -> "📁 Nouveau dossier créé";
            case INTERNAL_DOSSIER_COMPLETED -> "✅ Dossier terminé";
        };
    }

    private String resolveMessage(AlertType type, Map<String, Object> data) {
        String detail = data != null && data.containsKey("detail") ? data.get("detail").toString() : "";
        return switch (type) {
            case CLIENT_APPOINTMENT_CONFIRMATION -> "Votre rendez-vous a été confirmé. " + detail;
            case CLIENT_APPOINTMENT_REMINDER_J2 -> "Rappel : votre rendez-vous est dans 2 jours. " + detail;
            case CLIENT_APPOINTMENT_REMINDER_J1 -> "Rappel : votre rendez-vous est demain. " + detail;
            case CLIENT_QUOTE_SENT -> "Votre devis est disponible. " + detail;
            case CLIENT_INVOICE_SENT -> "Votre facture est disponible. " + detail;
            case CLIENT_PAYMENT_RECEIVED -> "Nous avons bien reçu votre paiement. " + detail;
            case CLIENT_GALLERY_READY -> "Votre galerie photo est prête à être consultée. " + detail;
            case CLIENT_INVOICE_OVERDUE -> "Une facture est en attente de paiement. " + detail;
            default -> type.getDescription() + " " + detail;
        };
    }
}
