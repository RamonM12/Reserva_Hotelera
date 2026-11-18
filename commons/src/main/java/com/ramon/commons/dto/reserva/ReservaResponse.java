package com.ramon.commons.dto.reserva;

import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.dto.huesped.HuespedResponse;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaResponse(
        Long id,
        HuespedResponse huesped,
        HabitacionResponse habitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        Integer numeroPersonas,
        BigDecimal precioNoche,
        BigDecimal precioTotal,
        EstadoReserva estado,
        EstadoRegistro estadoRegistro
) {}