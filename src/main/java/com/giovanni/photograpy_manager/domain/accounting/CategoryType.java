package com.giovanni.photograpy_manager.domain.accounting;

public enum CategoryType {
    REVENUE("Recette"),
    EXPENSE("Dépense");

    private final String label;
    CategoryType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
