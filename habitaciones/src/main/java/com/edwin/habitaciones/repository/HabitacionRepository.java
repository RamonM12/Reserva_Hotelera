package com.edwin.habitaciones.repository;

import com.edwin.habitaciones.entity.Habitacion;
import com.ramon.commons.enums.EstadoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    List<Habitacion> findByEstadoRegistro(EstadoRegistro estadoRegistro);

    Optional<Habitacion> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);

    boolean existsByNumeroAndEstadoRegistro(Long numero, EstadoRegistro estadoRegistro);

    boolean existsByNumeroAndEstadoRegistroAndIdNot(Long numero, EstadoRegistro estadoRegistro, Long id);
}