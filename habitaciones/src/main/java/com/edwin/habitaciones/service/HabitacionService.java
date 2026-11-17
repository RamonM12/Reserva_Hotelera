package com.edwin.habitaciones.service;


import com.ramon.commons.dto.habitacion.HabitacionRequest;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.service.CrudService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HabitacionService extends CrudService<HabitacionRequest, HabitacionResponse> {
    void actualizarDisponibilidadHabitacion(Long idHabitacion, Long idDisponibilidad);

    HabitacionResponse obtenerHabitacionSinValidarEstado(Long id);

}