package com.ramon.reservas.services;

import com.ramon.commons.clients.HabitacionClient;
import com.ramon.commons.clients.HuespedClient;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import com.ramon.commons.dto.huesped.HuespedResponse;
import com.ramon.commons.dto.reserva.ReservaRequest;
import com.ramon.commons.dto.reserva.ReservaResponse;
import com.ramon.commons.enums.EstadoHabitacion;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.EstadoReserva;
import com.ramon.commons.exceptions.ConflictException;
import com.ramon.commons.exceptions.DatoInvalidoException;
import com.ramon.commons.exceptions.RecursoNoEncontradoException;
import com.ramon.commons.exceptions.ServicioNoDisponibleException;
import com.ramon.reservas.entity.Reserva;
import com.ramon.reservas.mapper.ReservaMapper;
import com.ramon.reservas.repository.ReservaRepository;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ReservaServiceImpl implements ReservaService {

    // Reserva vigente = CONFIRMADA o EN_CURSO (Notion 6.4)
    private static final List<EstadoReserva> VIGENTES =
            List.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final HuespedClient huespedClient;
    private final HabitacionClient habitacionClient;

    /* ============================ CONSULTAS ============================ */

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listar() {
        log.info("Listando reservas activas");
        return reservaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(Long id) {
        log.info("Buscando reserva {}", id);
        return toResponse(buscarActiva(id));
    }

    /* ============================ CREAR ============================ */

    @Override
    public ReservaResponse registrar(ReservaRequest request) {
        log.info("Registrando reserva para huésped {} en habitación {}",
                request.idHuesped(), request.idHabitacion());

        validarFechas(request.fechaEntrada(), request.fechaSalida());

        // Paso 1: validar el huésped (Feign). Inexistente -> 404, ELIMINADO -> 409
        HuespedResponse huesped = obtenerHuesped(request.idHuesped());
        if (huesped.estadoRegistro() == EstadoRegistro.ELIMINADO) {
            throw new ConflictException("El huésped " + huesped.id() + " está eliminado");
        }

        // Validar la habitación (Feign). Inexistente -> 404, ELIMINADA o no DISPONIBLE -> 409
        HabitacionResponse habitacion = obtenerHabitacion(request.idHabitacion());
        if (habitacion.estadoRegistro() == EstadoRegistro.ELIMINADO) {
            throw new ConflictException("La habitación " + habitacion.id() + " está eliminada");
        }
        if (habitacion.estado() != EstadoHabitacion.DISPONIBLE) {
            throw new ConflictException("La habitación no está DISPONIBLE, está en " + habitacion.estado());
        }
        if (request.numeroPersonas() > habitacion.capacidad()) {
            throw new ConflictException("El número de personas excede la capacidad de la habitación ("
                    + habitacion.capacidad() + ")");
        }
        if (reservaRepository.existsByIdHabitacionAndEstadoIn(habitacion.id(), VIGENTES)) {
            throw new ConflictException("La habitación ya tiene una reserva vigente");
        }

        // Paso 2: cambiar la habitación de DISPONIBLE a OCUPADA (Feign)
        HabitacionResponse habitacionOcupada =
                cambiarEstadoHabitacion(habitacion.id(), EstadoHabitacion.OCUPADA);

        // Paso 3: calcular precios y guardar. Si falla, se compensa la habitación
        try {
            Reserva reserva = reservaMapper.requestToEntity(request);
            reserva.setPrecioNoche(habitacion.precio()); // copia del precio al crear
            reserva.setPrecioTotal(calcularTotal(habitacion.precio(),
                    reserva.getFechaEntrada(), reserva.getFechaSalida()));
            reserva = reservaRepository.saveAndFlush(reserva);
            return reservaMapper.entityToResponse(reserva, huesped, habitacionOcupada);
        } catch (RuntimeException e) {
            log.error("Falló al guardar la reserva, regresando la habitación a DISPONIBLE", e);
            cambiarEstadoHabitacion(habitacion.id(), EstadoHabitacion.DISPONIBLE); // compensación
            throw e;
        }
    }

    /* ============================ MODIFICAR ============================ */

    @Override
    public ReservaResponse actualizar(Long id, ReservaRequest request) {
        log.info("Actualizando reserva {}", id);
        Reserva reserva = buscarActiva(id);

        // Huésped y habitación nunca cambian (Notion 6.4) -> 409
        if (!reserva.getIdHuesped().equals(request.idHuesped())
                || !reserva.getIdHabitacion().equals(request.idHabitacion())) {
            throw new ConflictException(
                    "No se puede cambiar el huésped ni la habitación. Cancele la reserva y cree otra");
        }
        // numeroPersonas se fija al crear (Notion 4.3)
        if (!reserva.getNumeroPersonas().equals(request.numeroPersonas())) {
            throw new ConflictException("El número de personas no se puede modificar");
        }

        validarFechas(request.fechaEntrada(), request.fechaSalida());

        // Qué se puede modificar depende del estado (Notion 6.4)
        switch (reserva.getEstado()) {
            case CONFIRMADA -> {
                reserva.setFechaEntrada(request.fechaEntrada());
                reserva.setFechaSalida(request.fechaSalida());
            }
            case EN_CURSO -> {
                if (!reserva.getFechaEntrada().equals(request.fechaEntrada())) {
                    throw new ConflictException(
                            "Después del check-in solo se puede modificar la fecha de salida");
                }
                reserva.setFechaSalida(request.fechaSalida());
            }
            default -> throw new ConflictException(
                    "Una reserva " + reserva.getEstado() + " no se puede modificar");
        }

        // Al cambiar fechas se recalcula el total
        reserva.setPrecioTotal(calcularTotal(reserva.getPrecioNoche(),
                reserva.getFechaEntrada(), reserva.getFechaSalida()));

        return toResponse(reservaRepository.save(reserva));
    }

    /* ============================ CAMBIAR ESTADO ============================ */

    @Override
    public ReservaResponse cambiarEstado(Long idReserva, Integer idEstado) {
        // idEstado fuera de 1 a 4 -> 400
        EstadoReserva nuevo = EstadoReserva.fromId(idEstado);
        if (nuevo == null) {
            throw new DatoInvalidoException("Estado inválido: " + idEstado + ". Debe ser de 1 a 4");
        }

        // Reserva inexistente -> 404
        Reserva reserva = buscarActiva(idReserva);
        EstadoReserva actual = reserva.getEstado();
        log.info("Reserva {}: {} -> {}", idReserva, actual, nuevo);

        // 1 -> 2 Check-in: la habitación sigue OCUPADA
        if (actual == EstadoReserva.CONFIRMADA && nuevo == EstadoReserva.EN_CURSO) {
            reserva.setEstado(EstadoReserva.EN_CURSO);
            return toResponse(reservaRepository.save(reserva));
        }

        // 2 -> 3 Check-out: la habitación pasa a LIMPIEZA
        if (actual == EstadoReserva.EN_CURSO && nuevo == EstadoReserva.FINALIZADA) {
            return cambiarReservaYHabitacion(reserva, EstadoReserva.FINALIZADA, EstadoHabitacion.LIMPIEZA);
        }

        // 1 -> 4 Cancelación: la habitación pasa a DISPONIBLE
        if (actual == EstadoReserva.CONFIRMADA && nuevo == EstadoReserva.CANCELADA) {
            return cambiarReservaYHabitacion(reserva, EstadoReserva.CANCELADA, EstadoHabitacion.DISPONIBLE);
        }

        // Cualquier otra transición -> 409
        throw new ConflictException("Transición no permitida: " + actual + " -> " + nuevo);
    }

    // Cambia la habitación y la reserva. Si falla la reserva, regresa la habitación a OCUPADA
    private ReservaResponse cambiarReservaYHabitacion(Reserva reserva,
                                                      EstadoReserva nuevoEstado,
                                                      EstadoHabitacion estadoHabitacion) {
        HuespedResponse huesped = obtenerHuesped(reserva.getIdHuesped());
        HabitacionResponse habitacion = cambiarEstadoHabitacion(reserva.getIdHabitacion(), estadoHabitacion);
        try {
            reserva.setEstado(nuevoEstado);
            reserva = reservaRepository.saveAndFlush(reserva);
            return reservaMapper.entityToResponse(reserva, huesped, habitacion);
        } catch (RuntimeException e) {
            log.error("Falló al guardar la reserva, regresando la habitación a OCUPADA", e);
            cambiarEstadoHabitacion(reserva.getIdHabitacion(), EstadoHabitacion.OCUPADA); // compensación
            throw e;
        }
    }

    /* ============================ ELIMINAR ============================ */

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando reserva {}", id);
        Reserva reserva = buscarActiva(id);

        // Solo FINALIZADA o CANCELADA. No afecta a la habitación (Notion 6.4)
        if (reserva.getEstado() != EstadoReserva.FINALIZADA
                && reserva.getEstado() != EstadoReserva.CANCELADA) {
            throw new ConflictException("Solo se pueden eliminar reservas FINALIZADA o CANCELADA");
        }

        reserva.setEstadoRegistro(EstadoRegistro.ELIMINADO);
        reservaRepository.save(reserva);
    }

    /* ============================ REPORTES ============================ */

    // Todas las reservas, sin importar estado ni estadoRegistro (Notion 7)
    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> reporteHistorico() {
        log.info("Reporte histórico de reservas");
        return reservaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Agrupadas por EstadoReserva, incluidas las ELIMINADAS (Notion 7)
    @Override
    @Transactional(readOnly = true)
    public Map<EstadoReserva, List<ReservaResponse>> reportePorEstado() {
        log.info("Reporte de reservas por estado");
        Map<EstadoReserva, List<ReservaResponse>> reporte = new LinkedHashMap<>();
        for (EstadoReserva estado : EstadoReserva.values()) {
            reporte.put(estado, new ArrayList<>());
        }
        for (Reserva reserva : reservaRepository.findAll()) {
            reporte.get(reserva.getEstado()).add(toResponse(reserva));
        }
        return reporte;
    }

    /* ============================ INTERNOS ============================ */

    @Override
    @Transactional(readOnly = true)
    public boolean tieneReservasVigentes(Long idHuesped) {
        return reservaRepository.existsByIdHuespedAndEstadoIn(idHuesped, VIGENTES);
    }

    // Regresa numeroPersonas de la reserva vigente, o 0 si no hay
    @Override
    @Transactional(readOnly = true)
    public Integer personasReservaVigente(Long idHabitacion) {
        return reservaRepository.findFirstByIdHabitacionAndEstadoIn(idHabitacion, VIGENTES)
                .map(Reserva::getNumeroPersonas)
                .orElse(0);
    }

    /* ============================ AYUDANTES ============================ */

    private Reserva buscarActiva(Long id) {
        return reservaRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la reserva con id " + id));
    }

    // Solo se exige fechaEntrada < fechaSalida (Notion 4.3) -> 400
    private void validarFechas(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (!fechaEntrada.isBefore(fechaSalida)) {
            throw new DatoInvalidoException("La fecha de entrada debe ser menor que la fecha de salida");
        }
    }

    // noches = fechaSalida - fechaEntrada en días; total = noches x precioNoche
    private BigDecimal calcularTotal(BigDecimal precioNoche, LocalDate fechaEntrada, LocalDate fechaSalida) {
        long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        return precioNoche.multiply(BigDecimal.valueOf(noches));
    }

    // Arma la respuesta con los datos del huésped y la habitación (Notion 6.4)
    private ReservaResponse toResponse(Reserva reserva) {
        return reservaMapper.entityToResponse(reserva,
                obtenerHuesped(reserva.getIdHuesped()),
                obtenerHabitacion(reserva.getIdHabitacion()));
    }

    /* ---------- Llamadas Feign con su manejo de errores ---------- */

    private HuespedResponse obtenerHuesped(Long id) {
        try {
            return huespedClient.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("No se encontró el huésped con id " + id);
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("El servicio de huéspedes no está disponible");
        }
    }

    private HabitacionResponse obtenerHabitacion(Long id) {
        try {
            return habitacionClient.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("No se encontró la habitación con id " + id);
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("El servicio de habitaciones no está disponible");
        }
    }

    private HabitacionResponse cambiarEstadoHabitacion(Long id, EstadoHabitacion estado) {
        try {
            return habitacionClient.cambiarEstado(id, estado.getId());
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("No se encontró la habitación con id " + id);
        } catch (FeignException.Conflict e) {
            throw new ConflictException("La habitación no pudo cambiar a " + estado);
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException("El servicio de habitaciones no está disponible");
        }
    }
}