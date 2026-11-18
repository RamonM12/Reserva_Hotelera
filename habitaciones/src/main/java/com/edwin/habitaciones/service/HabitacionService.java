package com.edwin.habitaciones.service;

import com.ramon.commons.dto.habitacion.HabitacionRequest;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.service.CrudService;

public interface HabitacionService extends CrudService<HabitacionRequest, HabitacionResponse> {

    // Interno (Reservas): sin importar estadoRegistro
    HabitacionResponse obtenerHabitacionSinValidarEstado(Long id);

    // Manual (ADMIN): matriz Notion 4.2
    HabitacionResponse cambiarEstado(Long id, Integer idEstado);

    // Automático (Reservas): DISPONIBLE->OCUPADA, OCUPADA->LIMPIEZA, OCUPADA->DISPONIBLE
    HabitacionResponse cambiarEstadoInterno(Long id, Integer idEstado);
}