package com.giovanni.photograpy_manager.domain.client;

public enum EventType {
    MARIAGE("Mariage"),
    ANNIVERSAIRE("Anniversaire"),
    CORPORATE("Corporate / Pro"),
    PORTRAIT("Séance portrait"),
    BAPTEME("Baptême / Communion"),
    GROSSESSE("Grossesse / Naissance"),
    EVENEMENTIEL("Événementiel"),
    SUR_MESURE("Prestation sur mesure");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
