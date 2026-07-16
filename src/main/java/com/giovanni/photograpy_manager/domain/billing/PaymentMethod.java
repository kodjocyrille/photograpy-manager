package com.giovanni.photograpy_manager.domain.billing;

public enum PaymentMethod {
    CB("Carte bancaire"),
    VIREMENT("Virement"),
    ESPECES("Espèces"),
    CHEQUE("Chèque"),
    MOBILE_MONEY("Mobile Money");

    private final String label;
    PaymentMethod(String label) { this.label = label; }
    public String getLabel() { return label; }
}
