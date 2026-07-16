package com.giovanni.photograpy_manager.domain.billing;

public enum QuoteStatus {
    DRAFT("Brouillon"),
    SENT("Envoyé"),
    ACCEPTED("Accepté"),
    REJECTED("Rejeté");

    private final String label;
    QuoteStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
