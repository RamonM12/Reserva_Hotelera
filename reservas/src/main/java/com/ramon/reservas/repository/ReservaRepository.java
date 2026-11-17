package com.ramon.reservas.repository;

import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.EstadoReserva;
import com.ramon.reservas.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // GET /  -> solo ACTIVAS
    List<Reserva> findByEstadoRegistro(EstadoRegistro estadoRegistro);

    // GET /{id}, PUT, PATCH, DELETE -> solo ACTIVAS (ELIMINADA = 404)
    Optional<Reserva> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);

    // "Una habitación solo puede tener una reserva vigente (CONFIRMADA o EN_CURSO)" (Notion 6.4)
    boolean existsByIdHabitacionAndEstadoIn(Long idHabitacion, List<EstadoReserva> estados);

    // Para Huéspedes: "No se puede eliminar un huésped con reservas CONFIRMADA o EN_CURSO" (Notion 6.2)
    boolean existsByIdHuespedAndEstadoIn(Long idHuesped, List<EstadoReserva> estados);

    // Para Habitaciones: la capacidad no puede ser menor que numeroPersonas de la reserva vigente (Notion 4.3)
    Optional<Reserva> findFirstByIdHabitacionAndEstadoIn(Long idHabitacion, List<EstadoReserva> estados);
}