package com.ramon.commons.dto.huesped;

import com.ramon.commons.enums.TipoDocumento;
import jakarta.validation.constraints.*;

public record HuespedRequest(

        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es requerido")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        String apellido,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "El teléfono es requerido")
        @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener exactamente 10 dígitos")
        String telefono,

        @NotNull(message = "El tipo de documento es requerido")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es requerido")
        @Size(min = 8, max = 20, message = "El número de documento debe tener entre 8 y 20 caracteres")
        String numeroDocumento,

        @NotBlank(message = "La nacionalidad es requerida")
        String nacionalidad
) {}
