package com.ramon.commons.dto.reserva;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservaRequest(

        @NotNull(message = "El huésped es requerido")
        Long idHuesped,

        @NotNull(message = "La habitación es requerida")
        Long idHabitacion,

        @NotNull(message = "La fecha de entrada es requerida")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es requerida")
        LocalDate fechaSalida,

        @NotNull(message = "El número de personas es requerido")
        @Min(value = 1, message = "El número de personas debe ser al menos 1")
        Integer numeroPersonas
) {}