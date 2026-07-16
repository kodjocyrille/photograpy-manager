package com.giovanni.photograpy_manager.dto;

import com.giovanni.photograpy_manager.domain.appointment.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AppointmentForm {

    private Long id;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le type de RDV est obligatoire")
    private AppointmentType type;

    @NotNull(message = "La date/heure de début est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "La date/heure de fin est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    private String location;

    private Long photographerId;

    private String notes;
}
