package com.ramon.commons.dto.huesped;

import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.TipoDocumento;

public record HuespedResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String nacionalidad,
        EstadoRegistro estadoRegistro
) {}
