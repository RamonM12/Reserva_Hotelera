package com.edwin.habitaciones.service;

import com.edwin.habitaciones.entity.Habitacion;
import com.edwin.habitaciones.mapper.HabitacionMapper;
import com.edwin.habitaciones.repository.HabitacionRepository;
import com.ramon.commons.clients.ReservaClient;
import com.ramon.commons.dto.habitacion.HabitacionRequest;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.enums.EstadoHabitacion;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.exceptions.ConflictException;
import com.ramon.commons.exceptions.DatoInvalidoException;
import com.ramon.commons.exceptions.RecursoNoEncontradoException;
import com.ramon.commons.exceptions.ServicioNoDisponibleException;
import feign.FeignException;
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
    private final ReservaClient reservaClient;

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> listar() {
        log.info("Listando todas las habitaciones activas...");
        return habitacionRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(habitacionMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionResponse obtenerHabitacionSinValidarEstado(Long id) {
        log.info("Buscando habitación sin validar estado con id {}", id);
        return habitacionMapper.entidadAResponse(habitacionRepository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Habitación no encontrada con id: " + id)));
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

        if (habitacion.getEstadoHabitacion() == EstadoHabitacion.OCUPADA) {
            Integer personas = obtenerPersonasReservaVigente(id);
            if (request.capacidad() < personas) {
                throw new ConflictException("La capacidad no puede ser menor que el número de personas de la reserva vigente ("
                        + personas + ")");
            }
        }

        log.info("Actualizando habitación con id: {}", id);

        habitacion.actualizar(request.numero(), request.precio(), request.capacidad(), request.tipoHabitacion());
        habitacionRepository.saveAndFlush(habitacion);
        return habitacionMapper.entidadAResponse(habitacion);
    }

    @Override
    public void eliminar(Long id) {
        Habitacion habitacion = obtenerHabitacionActivaPorId(id);
        log.info("Eliminando lógicamente la habitación con id: {}", id);
        habitacion.eliminar(); // lanza 409 si está OCUPADA
        habitacionRepository.saveAndFlush(habitacion);
    }

    @Override
    public HabitacionResponse cambiarEstado(Long id, Integer idEstado) {
        EstadoHabitacion nuevo = obtenerEstado(idEstado);
        Habitacion habitacion = obtenerHabitacionActivaPorId(id);
        EstadoHabitacion actual = habitacion.getEstadoHabitacion();
        log.info("Cambio manual de habitación {}: {} -> {}", id, actual, nuevo);

        if (actual == EstadoHabitacion.OCUPADA) {
            throw new ConflictException("Una habitación OCUPADA no puede cambiar de estado manualmente");
        }
        if (nuevo == EstadoHabitacion.OCUPADA) {
            throw new ConflictException("No se puede cambiar manualmente una habitación a OCUPADA");
        }
        if (nuevo == actual) {
            throw new ConflictException("La habitación ya se encuentra en " + actual);
        }

        habitacion.cambiarEstado(nuevo);
        habitacionRepository.saveAndFlush(habitacion);
        return habitacionMapper.entidadAResponse(habitacion);
    }


    @Override
    public HabitacionResponse cambiarEstadoInterno(Long id, Integer idEstado) {
        EstadoHabitacion nuevo = obtenerEstado(idEstado);
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Habitación no encontrada con id: " + id));
        EstadoHabitacion actual = habitacion.getEstadoHabitacion();
        log.info("Cambio automático de habitación {}: {} -> {}", id, actual, nuevo);

        boolean valida =
                (actual == EstadoHabitacion.DISPONIBLE && nuevo == EstadoHabitacion.OCUPADA)
                        || (actual == EstadoHabitacion.OCUPADA && nuevo == EstadoHabitacion.LIMPIEZA)
                        || (actual == EstadoHabitacion.OCUPADA && nuevo == EstadoHabitacion.DISPONIBLE)
                        || (actual == EstadoHabitacion.LIMPIEZA && nuevo == EstadoHabitacion.OCUPADA);

        if (!valida) {
            throw new ConflictException("Transición no permitida: " + actual + " -> " + nuevo);
        }

        habitacion.cambiarEstado(nuevo);
        habitacionRepository.saveAndFlush(habitacion);
        return habitacionMapper.entidadAResponse(habitacion);
    }



    private Habitacion obtenerHabitacionActivaPorId(Long id) {
        log.info("Buscando habitación activa con id {}", id);
        return habitacionRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Habitación activa no encontrada con id: " + id));
    }


    private EstadoHabitacion obtenerEstado(Integer idEstado) {
        EstadoHabitacion estado = idEstado == null ? null : EstadoHabitacion.fromId(idEstado);
        if (estado == null) {
            throw new DatoInvalidoException("Estado inválido: " + idEstado + ". Debe ser de 1 a 4");
        }
        return estado;
    }

    private Integer obtenerPersonasReservaVigente(Long idHabitacion) {
        try {
            return reservaClient.personasReservaVigente(idHabitacion);
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("El servicio de reservas no está disponible"); // 503
        }
    }

    private void validarDatosUnicos(HabitacionRequest request) {
        if (habitacionRepository.existsByNumeroAndEstadoRegistro(request.numero(), EstadoRegistro.ACTIVO)) {
            throw new ConflictException("Ya existe una habitación activa registrada con el número: " + request.numero());
        }
    }

    private void validarCambiosUnicos(HabitacionRequest request, Long id) {
        if (habitacionRepository.existsByNumeroAndEstadoRegistroAndIdNot(request.numero(), EstadoRegistro.ACTIVO, id)) {
            throw new ConflictException("Ya existe otra habitación activa registrada con el número: " + request.numero());
        }
    }
}