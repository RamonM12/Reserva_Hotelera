package com.edwin.habitaciones.mapper;

import com.edwin.habitaciones.entity.Habitacion;
import com.ramon.commons.dto.habitacion.HabitacionRequest;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.mapper.CommonMapper;
import org.springframework.stereotype.Component;

@Component
public class HabitacionMapper implements CommonMapper<HabitacionRequest, HabitacionResponse, Habitacion> {

    @Override
    public Habitacion requestAEntidad(HabitacionRequest request) {
        return request == null
                ? null
                : Habitacion.crear(
                request.numero(),
                request.precio(),
                request.capacidad(),
                request.tipoHabitacion()
        );
    }

    @Override
    public HabitacionResponse entidadAResponse(Habitacion entidad) {
        return entidad == null
                ? null
                : new HabitacionResponse(
                entidad.getId(),
                entidad.getNumero(),
                entidad.getPrecio(),
                entidad.getCapacidad(),
                entidad.getEstadoHabitacion(),
                entidad.getEstadoRegistro(),
                entidad.getTipoHabitacion()
        );
    }
}