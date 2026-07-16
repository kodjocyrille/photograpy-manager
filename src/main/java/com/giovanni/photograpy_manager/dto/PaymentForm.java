package com.giovanni.photograpy_manager.dto;

import com.giovanni.photograpy_manager.domain.billing.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentForm {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private PaymentMethod method;

    @NotNull(message = "La date est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;
}
