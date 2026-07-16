package com.giovanni.photograpy_manager.domain.workspace;

public enum DossierStatus {
    WAITING_SESSION("En attente de séance"),
    RETOUCHING("Retouche en cours"),
    AWAITING_VALIDATION("En attente validation"),
    CORRECTIONS_REQUESTED("Corrections demandées"),
    COMPLETED("Terminé");

    private final String label;
    DossierStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
