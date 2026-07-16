package com.giovanni.photograpy_manager.domain.alert;

public enum EmailStatus {
    SENT("Envoyé"),
    FAILED("Échec"),
    PENDING("En attente");

    private final String label;
    EmailStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
