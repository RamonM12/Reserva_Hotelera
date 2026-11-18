package com.ramon.reservas.mapper;

import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.dto.huesped.HuespedResponse;
import com.ramon.commons.dto.reserva.ReservaRequest;
import com.ramon.commons.dto.reserva.ReservaResponse;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.EstadoReserva;
import com.ramon.reservas.entity.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    // Entity -> Response
    //El huésped y la habitación ya llegan consultados por Feign desde el service
    public ReservaResponse entityToResponse(Reserva reserva,
                                            HuespedResponse huesped,
                                            HabitacionResponse habitacion) {
        if (reserva == null) return null;
        return new ReservaResponse(
                reserva.getId(),
                huesped,
                habitacion,
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getNumeroPersonas(),
                reserva.getPrecioNoche(),
                reserva.getPrecioTotal(),
                reserva.getEstado(),
                reserva.getEstadoRegistro());
    }

    // Request -> Entity
    // Los precios NO vienen del cliente: los calcula el service (Notion 4.3)
    public Reserva requestToEntity(ReservaRequest request) {
        if (request == null) return null;
        Reserva reserva = new Reserva();
        reserva.setIdHuesped(request.idHuesped());
        reserva.setIdHabitacion(request.idHabitacion());
        reserva.setFechaEntrada(request.fechaEntrada());
        reserva.setFechaSalida(request.fechaSalida());
        reserva.setNumeroPersonas(request.numeroPersonas());
        reserva.setEstado(EstadoReserva.CONFIRMADA);      // Inicial: CONFIRMADA
        reserva.setEstadoRegistro(EstadoRegistro.ACTIVO); // Inicial: ACTIVO
        return reserva;
    }
}