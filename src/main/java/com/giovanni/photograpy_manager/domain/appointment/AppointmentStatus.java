package com.giovanni.photograpy_manager.domain.appointment;

public enum AppointmentStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmé"),
    POSTPONED("Reporté"),
    CANCELLED("Annulé"),
    DONE("Terminé");

    private final String label;
    AppointmentStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
