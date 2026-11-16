package com.ramon.huespedes.repository;

import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.TipoDocumento;
import com.ramon.huespedes.entity.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    List<Huesped> findByEstadoRegistro(EstadoRegistro estadoRegistro);

    Optional<Huesped> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);

    boolean existsByEmailAndEstadoRegistroAndIdNot(String email, EstadoRegistro estadoRegistro, Long id);

    boolean existsByTelefonoAndEstadoRegistroAndIdNot(String telefono, EstadoRegistro estadoRegistro, Long id);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndEstadoRegistroAndIdNot(
            TipoDocumento tipoDocumento, String numeroDocumento, EstadoRegistro estadoRegistro, Long id);
}