package com.ramon.commons.dto.habitacion;

import com.ramon.commons.enums.EstadoHabitacion;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.TipoHabitacion;

import java.math.BigDecimal;

public record HabitacionResponse(
        Long id,
        Integer numero,
        TipoHabitacion tipo,
        BigDecimal precio,
        Integer capacidad,
        EstadoHabitacion estado,
        EstadoRegistro estadoRegistro
) {}