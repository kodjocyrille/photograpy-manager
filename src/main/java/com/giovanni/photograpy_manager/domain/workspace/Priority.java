package com.giovanni.photograpy_manager.domain.workspace;

public enum Priority {
    NORMAL("Normal"),
    URGENT("Urgent"),
    VIP("VIP");

    private final String label;
    Priority(String label) { this.label = label; }
    public String getLabel() { return label; }
}
