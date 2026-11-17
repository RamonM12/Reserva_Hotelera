package com.ramon.commons.dto.habitacion;

import com.ramon.commons.enums.TipoHabitacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Datos necesarios para registrar o actualizar una habitación")
public record HabitacionRequest(

        @Schema(description = "Número de la habitación. Debe ser entero y mayor a 0", example = "101")
        @NotNull(message = "El número de habitación es requerido")
        @Min(value = 1, message = "El número de habitación debe ser mayor a 0")
        Long numero,

        @Schema(description = "Precio por noche de la habitación", example = "1200.50")
        @NotNull(message = "El precio es requerido")
        @DecimalMin(value = "0.01", message = "El precio de la habitación debe ser mayor a 0")
        BigDecimal precio,

        @Schema(description = "Capacidad máxima de personas en la habitación", example = "2")
        @NotNull(message = "La capacidad es requerida")
        @Min(value = 1, message = "La capacidad mínima de la habitación debe ser de al menos 1 persona")
        Long capacidad,

        @Schema(description = "Tipo de habitación (Ej. INDIVIDUAL, DOBLE, SUITE)", example = "DOBLE")
        @NotNull(message = "El tipo de habitación es obligatorio")
        TipoHabitacion tipoHabitacion
) {
}