package com.giovanni.photograpy_manager.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuoteForm {
    private Long id;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    private String validUntil; // yyyy-MM-dd

    private List<LineItem> lines = new ArrayList<>();

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LineItem {
        private String description;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
