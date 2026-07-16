package com.giovanni.photograpy_manager.domain.billing;

public enum PrestationStatus {
    EN_ATTENTE("En attente"),
    EN_COURS("En cours"),
    TERMINEE("Terminée"),
    ANNULEE("Annulée");

    private final String label;

    PrestationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
