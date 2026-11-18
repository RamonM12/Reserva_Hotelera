package com.ramon.reservas.services;

import com.ramon.commons.dto.reserva.ReservaRequest;
import com.ramon.commons.dto.reserva.ReservaResponse;
import com.ramon.commons.enums.EstadoReserva;

import java.util.List;
import java.util.Map;

public interface ReservaService {

    List<ReservaResponse> listar();

    ReservaResponse buscarPorId(Long id);

    ReservaResponse registrar(ReservaRequest request);

    ReservaResponse actualizar(Long id, ReservaRequest request);

    ReservaResponse cambiarEstado(Long idReserva, Integer idEstado);

    void eliminar(Long id);

    // Reportes (solo ADMIN)
    List<ReservaResponse> reporteHistorico();

    Map<EstadoReserva, List<ReservaResponse>> reportePorEstado();

    // Internos: los usan Huéspedes y Habitaciones por Feign
    boolean tieneReservasVigentes(Long idHuesped);

    Integer personasReservaVigente(Long idHabitacion);
}