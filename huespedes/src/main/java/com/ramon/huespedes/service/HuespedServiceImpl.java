package com.ramon.huespedes.service;


import com.ramon.commons.dto.huesped.HuespedRequest;
import com.ramon.commons.dto.huesped.HuespedResponse;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.exceptions.ConflictException;
import com.ramon.commons.exceptions.RecursoNoEncontradoException;
import com.ramon.huespedes.entity.Huesped;
import com.ramon.huespedes.mapper.HuespedMapper;
import com.ramon.huespedes.repository.HuespedRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class HuespedServiceImpl implements HuespedService {

    private final HuespedRepository huespedRepository;
    private final HuespedMapper huespedMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HuespedResponse> listar() {
        log.info("Listando huéspedes activos");
        return huespedRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(huespedMapper::entityToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HuespedResponse buscarPorId(Long id) {
        log.info("Buscando huésped {}", id);
        return huespedMapper.entityToResponse(buscarActivo(id));
    }

    @Override
    @Transactional(readOnly = true)
    public HuespedResponse buscarInterno(Long id) {
        log.info("Consulta interna del huésped {}", id);
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el huésped con id " + id));
        return huespedMapper.entityToResponse(huesped);
    }

    @Override
    public HuespedResponse registrar(HuespedRequest request) {
        Huesped huesped = huespedMapper.requestToEntity(request);
        log.info("Registrando huésped {}", huesped.getEmail());

        validarDuplicados(huesped, 0L);

        return huespedMapper.entityToResponse(huespedRepository.save(huesped));
    }

    @Override
    public HuespedResponse actualizar(Long id, HuespedRequest request) {
        log.info("Actualizando huésped {}", id);
        Huesped huesped = buscarActivo(id);
        Huesped datos = huespedMapper.requestToEntity(request);

        validarDuplicados(datos, id);

        huesped.setNombre(datos.getNombre());
        huesped.setApellido(datos.getApellido());
        huesped.setEmail(datos.getEmail());
        huesped.setTelefono(datos.getTelefono());
        huesped.setTipoDocumento(datos.getTipoDocumento());
        huesped.setNumeroDocumento(datos.getNumeroDocumento());
        huesped.setNacionalidad(datos.getNacionalidad());

        return huespedMapper.entityToResponse(huespedRepository.save(huesped));
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando huésped {}", id);
        Huesped huesped = buscarActivo(id);

        // TODO: cuando exista Reservas, preguntar por Feign si el huésped

        huesped.setEstadoRegistro(EstadoRegistro.ELIMINADO);
        huespedRepository.save(huesped);
    }

    private Huesped buscarActivo(Long id) {
        return huespedRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el huésped con id " + id));
    }

    private void validarDuplicados(Huesped huesped, Long idExcluir) {
        if (huespedRepository.existsByEmailAndEstadoRegistroAndIdNot(
                huesped.getEmail(), EstadoRegistro.ACTIVO, idExcluir)) {
            throw new ConflictException("Ya existe un huésped activo con el email " + huesped.getEmail());
        }
        if (huespedRepository.existsByTelefonoAndEstadoRegistroAndIdNot(
                huesped.getTelefono(), EstadoRegistro.ACTIVO, idExcluir)) {
            throw new ConflictException("Ya existe un huésped activo con el teléfono " + huesped.getTelefono());
        }
        if (huespedRepository.existsByTipoDocumentoAndNumeroDocumentoAndEstadoRegistroAndIdNot(
                huesped.getTipoDocumento(), huesped.getNumeroDocumento(), EstadoRegistro.ACTIVO, idExcluir)) {
            throw new ConflictException("Ya existe un huésped activo con ese documento");
        }
    }
}
