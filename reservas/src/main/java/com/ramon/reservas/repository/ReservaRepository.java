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


    List<Reserva> findByEstadoRegistro(EstadoRegistro estadoRegistro);


    Optional<Reserva> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);


    boolean existsByIdHabitacionAndEstadoIn(Long idHabitacion, List<EstadoReserva> estados);

    boolean existsByIdHuespedAndEstadoIn(Long idHuesped, List<EstadoReserva> estados);

   Optional<Reserva> findFirstByIdHabitacionAndEstadoIn(Long idHabitacion, List<EstadoReserva> estados);
}