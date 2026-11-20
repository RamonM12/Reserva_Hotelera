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


    private static final List<EstadoReserva> VIGENTES =
            List.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final HuespedClient huespedClient;
    private final HabitacionClient habitacionClient;


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

    @Override
    public ReservaResponse registrar(ReservaRequest request) {
        log.info("Registrando reserva para huésped {} en habitación {}",
                request.idHuesped(), request.idHabitacion());

        validarFechas(request.fechaEntrada(), request.fechaSalida());


        HuespedResponse huesped = obtenerHuesped(request.idHuesped());
        if (huesped.estadoRegistro() == EstadoRegistro.ELIMINADO) {
            throw new ConflictException("El huésped " + huesped.id() + " está eliminado");
        }

        HabitacionResponse habitacion = obtenerHabitacion(request.idHabitacion());
        if (habitacion.estadoRegistro() == EstadoRegistro.ELIMINADO) {
            throw new ConflictException("La habitación " + habitacion.id() + " está eliminada");
        }
        if (habitacion.estadoHabitacion() != EstadoHabitacion.DISPONIBLE) {
            throw new ConflictException("La habitación no está DISPONIBLE, está en " + habitacion.estadoHabitacion());
        }
        if (request.numeroPersonas() > habitacion.capacidad()) {
            throw new ConflictException("El número de personas excede la capacidad de la habitación ("
                    + habitacion.capacidad() + ")");
        }
        if (reservaRepository.existsByIdHabitacionAndEstadoIn(habitacion.id(), VIGENTES)) {
            throw new ConflictException("La habitación ya tiene una reserva vigente");
        }


        HabitacionResponse habitacionOcupada =
                cambiarEstadoHabitacion(habitacion.id(), EstadoHabitacion.OCUPADA);


        try {
            Reserva reserva = reservaMapper.requestToEntity(request);
            reserva.setPrecioNoche(habitacion.precio());
            reserva.setPrecioTotal(calcularTotal(habitacion.precio(),
                    reserva.getFechaEntrada(), reserva.getFechaSalida()));
            reserva = reservaRepository.saveAndFlush(reserva);
            return reservaMapper.entityToResponse(reserva, huesped, habitacionOcupada);
        } catch (RuntimeException e) {
            log.error("Falló al guardar la reserva, regresando la habitación a DISPONIBLE", e);
            cambiarEstadoHabitacion(habitacion.id(), EstadoHabitacion.DISPONIBLE);
            throw e;
        }
    }

    @Override
    public ReservaResponse actualizar(Long id, ReservaRequest request) {
        log.info("Actualizando reserva {}", id);
        Reserva reserva = buscarActiva(id);


        if (!reserva.getIdHuesped().equals(request.idHuesped())
                || !reserva.getIdHabitacion().equals(request.idHabitacion())) {
            throw new ConflictException(
                    "No se puede cambiar el huésped ni la habitación. Cancele la reserva y cree otra");
        }

        if (!reserva.getNumeroPersonas().equals(request.numeroPersonas())) {
            throw new ConflictException("El número de personas no se puede modificar");
        }

        validarFechas(request.fechaEntrada(), request.fechaSalida());


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


        reserva.setPrecioTotal(calcularTotal(reserva.getPrecioNoche(),
                reserva.getFechaEntrada(), reserva.getFechaSalida()));

        return toResponse(reservaRepository.save(reserva));
    }



    @Override
    public ReservaResponse cambiarEstado(Long idReserva, Integer idEstado) {

        EstadoReserva nuevo = EstadoReserva.fromId(idEstado);
        if (nuevo == null) {
            throw new DatoInvalidoException("Estado inválido: " + idEstado + ". Debe ser de 1 a 4");
        }


        Reserva reserva = buscarActiva(idReserva);
        EstadoReserva actual = reserva.getEstado();
        log.info("Reserva {}: {} -> {}", idReserva, actual, nuevo);

        if (actual == EstadoReserva.CONFIRMADA && nuevo == EstadoReserva.EN_CURSO) {
            reserva.setEstado(EstadoReserva.EN_CURSO);
            return toResponse(reservaRepository.save(reserva));
        }


        if (actual == EstadoReserva.EN_CURSO && nuevo == EstadoReserva.FINALIZADA) {
            return cambiarReservaYHabitacion(reserva, EstadoReserva.FINALIZADA, EstadoHabitacion.LIMPIEZA);
        }


        if (actual == EstadoReserva.CONFIRMADA && nuevo == EstadoReserva.CANCELADA) {
            return cambiarReservaYHabitacion(reserva, EstadoReserva.CANCELADA, EstadoHabitacion.DISPONIBLE);
        }


        throw new ConflictException("Transición no permitida: " + actual + " -> " + nuevo);
    }


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

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando reserva {}", id);
        Reserva reserva = buscarActiva(id);

        if (reserva.getEstado() != EstadoReserva.FINALIZADA
                && reserva.getEstado() != EstadoReserva.CANCELADA) {
            throw new ConflictException("Solo se pueden eliminar reservas FINALIZADA o CANCELADA");
        }

        reserva.setEstadoRegistro(EstadoRegistro.ELIMINADO);
        reservaRepository.save(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> reporteHistorico() {
        log.info("Reporte histórico de reservas");
        return reservaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

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


    @Override
    @Transactional(readOnly = true)
    public boolean tieneReservasVigentes(Long idHuesped) {
        return reservaRepository.existsByIdHuespedAndEstadoIn(idHuesped, VIGENTES);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer personasReservaVigente(Long idHabitacion) {
        return reservaRepository.findFirstByIdHabitacionAndEstadoIn(idHabitacion, VIGENTES)
                .map(Reserva::getNumeroPersonas)
                .orElse(0);
    }


    private Reserva buscarActiva(Long id) {
        return reservaRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la reserva con id " + id));
    }

    private void validarFechas(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (!fechaEntrada.isBefore(fechaSalida)) {
            throw new DatoInvalidoException("La fecha de entrada debe ser menor que la fecha de salida");
        }
    }

    private BigDecimal calcularTotal(BigDecimal precioNoche, LocalDate fechaEntrada, LocalDate fechaSalida) {
        long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        return precioNoche.multiply(BigDecimal.valueOf(noches));
    }

    private ReservaResponse toResponse(Reserva reserva) {
        return reservaMapper.entityToResponse(reserva,
                obtenerHuesped(reserva.getIdHuesped()),
                obtenerHabitacion(reserva.getIdHabitacion()));
    }


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