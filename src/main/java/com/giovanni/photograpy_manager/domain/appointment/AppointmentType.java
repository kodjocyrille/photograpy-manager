package com.giovanni.photograpy_manager.domain.appointment;

public enum AppointmentType {
    CONSULTATION("Consultation initiale"),
    SEANCE("Séance photo"),
    REMISE("Remise des livrables"),
    SUIVI("Suivi");

    private final String label;
    AppointmentType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
