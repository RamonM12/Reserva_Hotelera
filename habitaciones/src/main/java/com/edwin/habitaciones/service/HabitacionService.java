package com.edwin.habitaciones.service;

import com.ramon.commons.dto.habitacion.HabitacionRequest;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.service.CrudService;

public interface HabitacionService extends CrudService<HabitacionRequest, HabitacionResponse> {

    HabitacionResponse obtenerHabitacionSinValidarEstado(Long id);

    HabitacionResponse cambiarEstado(Long id, Integer idEstado);

    HabitacionResponse cambiarEstadoInterno(Long id, Integer idEstado);
}