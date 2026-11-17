package com.ramon.commons.clients;

import com.ramon.commons.dto.habitacion.HabitacionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "habitaciones")
public interface HabitacionClient {
        @GetMapping("/{id}")
        HabitacionResponse obtenerHabitacionActivoPorId(@PathVariable Long id);

        @GetMapping("/id-paciente/{id}")
        HabitacionResponse obtenerPacienteSinEstadoPorId(@PathVariable Long id);
}
