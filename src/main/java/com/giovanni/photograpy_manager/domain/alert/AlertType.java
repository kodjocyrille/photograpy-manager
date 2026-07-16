package com.giovanni.photograpy_manager.domain.alert;

/**
 * Types d'alertes supportées par le système.
 * CLIENT = alertes envoyées aux clients
 * INTERNAL = alertes internes (équipe)
 */
public enum AlertType {
    // --- Alertes Client (8 types) ---
    CLIENT_APPOINTMENT_CONFIRMATION("Confirmation de RDV", "CLIENT", "Envoyée au client après confirmation d'un RDV"),
    CLIENT_APPOINTMENT_REMINDER_J2("Rappel RDV J-2", "CLIENT", "Rappel automatique 2 jours avant le RDV"),
    CLIENT_APPOINTMENT_REMINDER_J1("Rappel RDV J-1", "CLIENT", "Rappel automatique la veille du RDV"),
    CLIENT_QUOTE_SENT("Devis envoyé", "CLIENT", "Notification d'envoi de devis"),
    CLIENT_INVOICE_SENT("Facture envoyée", "CLIENT", "Notification d'envoi de facture"),
    CLIENT_PAYMENT_RECEIVED("Paiement reçu", "CLIENT", "Confirmation de réception de paiement"),
    CLIENT_GALLERY_READY("Galerie disponible", "CLIENT", "Notification de mise à disposition de la galerie photo"),
    CLIENT_INVOICE_OVERDUE("Facture en retard", "CLIENT", "Relance pour facture impayée"),

    // --- Alertes Prestation Client (2 types) ---
    CLIENT_PRESTATION_REMINDER_J2("Rappel prestation J-2", "CLIENT", "Rappel automatique 2 jours avant la prestation"),
    CLIENT_PRESTATION_REMINDER_J1("Rappel prestation J-1", "CLIENT", "Rappel automatique la veille de la prestation"),

    // --- Alertes Internes (6 + 1 types) ---
    INTERNAL_NEW_APPOINTMENT("Nouveau RDV", "INTERNAL", "Notification équipe : nouveau RDV planifié"),
    INTERNAL_APPOINTMENT_TOMORROW("RDV demain", "INTERNAL", "Rappel : RDV programmé demain"),
    INTERNAL_PRESTATION_TOMORROW("Prestation demain", "INTERNAL", "Rappel interne : prestation planifiée demain"),
    INTERNAL_INVOICE_OVERDUE_J5("Facture impayée J+5", "INTERNAL", "Alerte : facture non réglée depuis 5 jours"),
    INTERNAL_INVOICE_OVERDUE_J15("Facture impayée J+15", "INTERNAL", "Alerte : facture non réglée depuis 15 jours"),
    INTERNAL_DOSSIER_CREATED("Nouveau dossier", "INTERNAL", "Notification : dossier créé dans le workspace"),
    INTERNAL_DOSSIER_COMPLETED("Dossier terminé", "INTERNAL", "Notification : dossier marqué comme terminé");

    private final String label;
    private final String category; // CLIENT or INTERNAL
    private final String description;

    AlertType(String label, String category, String description) {
        this.label = label;
        this.category = category;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public boolean isClient() { return "CLIENT".equals(category); }
    public boolean isInternal() { return "INTERNAL".equals(category); }
}
