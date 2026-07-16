package com.giovanni.photograpy_manager.domain.workspace;

public enum DossierColumn {
    TO_PROCESS("À traiter"),
    DONE("Terminé");

    private final String label;
    DossierColumn(String label) { this.label = label; }
    public String getLabel() { return label; }
}
