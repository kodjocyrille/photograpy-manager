package com.giovanni.photograpy_manager.event;

import com.giovanni.photograpy_manager.domain.alert.AlertType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.Map;

/**
 * Domain event déclenché quand une alerte doit être envoyée.
 * Le listener EmailService traite cet event.
 */
@Getter
public class AlertEvent extends ApplicationEvent {
    private final AlertType alertType;
    private final String recipientEmail;
    private final String recipientName;
    private final Map<String, Object> data; // Données dynamiques pour le template

    public AlertEvent(Object source, AlertType alertType, String recipientEmail,
                      String recipientName, Map<String, Object> data) {
        super(source);
        this.alertType = alertType;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.data = data;
    }
}
