package com.giovanni.photograpy_manager.domain.accounting;

public enum EntryType {
    CREDIT("Crédit (recette)"),
    DEBIT("Débit (dépense)");

    private final String label;
    EntryType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
