package com.edwin.habitaciones.service;

import com.edwin.habitaciones.entity.Habitacion;
import com.edwin.habitaciones.mapper.HabitacionMapper;
import com.edwin.habitaciones.repository.HabitacionRepository;
import com.ramon.commons.dto.habitacion.HabitacionRequest;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.exceptions.ConflictException;
import com.ramon.commons.exceptions.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final HabitacionMapper habitacionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> listar() {
        log.info("Listando todas las habitaciones activas...");
        return habitacionRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(habitacionMapper::entidadAResponse).toList();
    }

    @Override
    public void actualizarDisponibilidadHabitacion(Long idHabitacion, Long idDisponibilidad) {

    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionResponse obtenerHabitacionSinValidarEstado(Long id) {
        log.info("Buscando habitación sin validar estado con id {}", id);
        return habitacionMapper.entidadAResponse(habitacionRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Habitación sin validar estado, no encontrada con id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionResponse obtenerPorId(Long id) {
        return habitacionMapper.entidadAResponse(obtenerHabitacionActivaPorId(id));
    }

    @Override
    public HabitacionResponse registrar(HabitacionRequest request) {
        log.info("Registrando nueva habitación con número: {}", request.numero());

        validarDatosUnicos(request);

        Habitacion habitacion = habitacionMapper.requestAEntidad(request);

        habitacionRepository.saveAndFlush(habitacion);

        log.info("Nueva habitación registrada correctamente con ID: {}", habitacion.getId());

        return habitacionMapper.entidadAResponse(habitacion);
    }

    @Override
    public HabitacionResponse actualizar(HabitacionRequest request, Long id) {
        Habitacion habitacion = obtenerHabitacionActivaPorId(id);

        validarCambiosUnicos(request, id);

        log.info("Actualizando habitación con id: {}", id);

        habitacion.actualizar(
                request.numero(),
                request.precio(),
                request.capacidad(),
                request.tipoHabitacion()
        );

        habitacionRepository.saveAndFlush(habitacion);

        log.info("Habitación con id: {} actualizada correctamente", id);

        return habitacionMapper.entidadAResponse(habitacion);
    }

    @Override
    public void eliminar(Long id) {
        Habitacion habitacion = obtenerHabitacionActivaPorId(id);

        log.info("Eliminando lógicamente la habitación con id: {}", id);

        habitacion.eliminar();

        habitacionRepository.saveAndFlush(habitacion);

        log.info("Habitación eliminada exitosamente");
    }

    private Habitacion obtenerHabitacionActivaPorId(Long id) {
        log.info("Buscando habitación activa con id {}", id);
        return habitacionRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Habitación activa no encontrada con id: " + id));
    }

    private void validarDatosUnicos(HabitacionRequest request) {
        log.info("Validando número de habitación único...");
        if (habitacionRepository.existsByNumeroAndEstadoRegistro(request.numero(), EstadoRegistro.ACTIVO)) {
            throw new ConflictException("Ya existe una habitación activa registrada con el número: " + request.numero());
        }
    }

    private void validarCambiosUnicos(HabitacionRequest request, Long id) {
        log.info("Validando cambio en número de habitación único...");
        if (habitacionRepository.existsByNumeroAndEstadoRegistroAndIdNot(request.numero(), EstadoRegistro.ACTIVO, id)) {
            throw new ConflictException("Ya existe otra habitación activa registrada con el número: " + request.numero());
        }
    }
}