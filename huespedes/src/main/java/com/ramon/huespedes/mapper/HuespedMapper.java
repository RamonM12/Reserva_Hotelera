package com.ramon.huespedes.mapper;
import com.ramon.commons.dto.huesped.HuespedRequest;
import com.ramon.commons.dto.huesped.HuespedResponse;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.utils.StringCustomUtils;
import com.ramon.huespedes.entity.Huesped;
import org.springframework.stereotype.Component;

@Component
public class HuespedMapper {

    // Entity -> Response
    public HuespedResponse entityToResponse(Huesped huesped) {
        if (huesped == null) return null;
        return new HuespedResponse(
                huesped.getId(),
                huesped.getNombre(),
                huesped.getApellido(),
                huesped.getEmail(),
                huesped.getTelefono(),
                huesped.getTipoDocumento(),
                huesped.getNumeroDocumento(),
                huesped.getNacionalidad(),
                huesped.getEstadoRegistro());
    }


    public Huesped requestToEntity(HuespedRequest request) {
        if (request == null) return null;
        Huesped huesped = new Huesped();
        huesped.setNombre(StringCustomUtils.normalizarTexto(request.nombre()));
        huesped.setApellido(StringCustomUtils.normalizarTexto(request.apellido()));
        huesped.setEmail(StringCustomUtils.normalizarEmail(request.email()));
        huesped.setTelefono(StringCustomUtils.normalizarTelefono(request.telefono()));
        huesped.setTipoDocumento(request.tipoDocumento());
        huesped.setNumeroDocumento(StringCustomUtils.normalizarTexto(request.numeroDocumento()));
        huesped.setNacionalidad(StringCustomUtils.normalizarTexto(request.nacionalidad()));
        huesped.setEstadoRegistro(EstadoRegistro.ACTIVO);
        return huesped;
    }
}