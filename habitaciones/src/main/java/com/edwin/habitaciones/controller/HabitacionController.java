package com.edwin.habitaciones.controller;

import com.edwin.habitaciones.service.HabitacionService;
import com.ramon.commons.controller.CrudController;
import com.ramon.commons.docs.ProblemaDoc;
import com.ramon.commons.dto.habitacion.HabitacionRequest;
import com.ramon.commons.dto.habitacion.HabitacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "API Habitaciones", description = "Métodos para la gestión de habitaciones")
public class HabitacionController extends CrudController<HabitacionRequest, HabitacionResponse, HabitacionService> {

    public HabitacionController(HabitacionService service) {
        super(service);
    }

    @GetMapping("/id-habitacion/{id}")
    @Operation(summary = "Obtener habitación por ID sin importar el estado del registro")
    @ApiResponse(responseCode = "200", description = "Habitación encontrada exitosamente")
    @ApiResponse(responseCode = "404", description = "La habitación no existe",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemaDoc.class)))
    public ResponseEntity<HabitacionResponse> obtenerHabitacionSinValidarEstado(
            @PathVariable @Positive(message = "El ID debe ser positivo") Long id
    ) {
        return ResponseEntity.ok(service.obtenerHabitacionSinValidarEstado(id));
    }
}
