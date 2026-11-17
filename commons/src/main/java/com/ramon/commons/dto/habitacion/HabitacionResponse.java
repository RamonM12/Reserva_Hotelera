package com.ramon.commons.dto.habitacion;

import com.ramon.commons.enums.EstadoHabitacion;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.TipoHabitacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Respuesta con los datos detallados de la habitación")
public record HabitacionResponse(
        @Schema(description = "ID único de la habitación", example = "1")
        Long id,

        @Schema(description = "Número de la habitación", example = "101")
        Long numero,

        @Schema(description = "Precio de la habitación", example = "1200.50")
        BigDecimal precio,

        @Schema(description = "Capacidad de la habitación", example = "2")
        Long capacidad,

        @Schema(description = "Estado actual operativo de la habitación", example = "DISPONIBLE")
        EstadoHabitacion estadoHabitacion,

        @Schema(description = "Estado de registro en el sistema", example = "ACTIVO")
        EstadoRegistro estadoRegistro,

        @Schema(description = "Tipo de habitación", example = "DOBLE")
        TipoHabitacion tipoHabitacion
) {
}