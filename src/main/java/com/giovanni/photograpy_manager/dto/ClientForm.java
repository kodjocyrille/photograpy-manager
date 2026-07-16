package com.giovanni.photograpy_manager.dto;

import com.giovanni.photograpy_manager.domain.client.EventType;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ClientForm {

    private Long id;

    @NotBlank(message = "Le nom complet est obligatoire")
    private String fullName;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String phone;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotNull(message = "Le type de prestation est obligatoire")
    private EventType serviceType;

    @NotNull(message = "La date de l'événement est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
}
