package com.giovanni.photograpy_manager.domain.billing;

public enum InvoiceStatus {
    DRAFT("Brouillon"),
    SENT("Envoyée"),
    PARTIALLY_PAID("Partiellement payée"),
    PAID("Payée"),
    OVERDUE("En retard");

    private final String label;
    InvoiceStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
