package com.ramon.commons.clients;


import com.ramon.commons.dto.habitacion.HabitacionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "habitaciones", path = "/api/habitaciones")
public interface HabitacionClient {

    // Endpoint interno: devuelve la habitación aunque esté ELIMINADA (Notion 6.3)
    @GetMapping("/id-habitacion/{id}")
    HabitacionResponse buscarPorId(@PathVariable("id") Long id);

    // Endpoint interno: cambio automático de estado por reservas
    // (DISPONIBLE->OCUPADA, OCUPADA->LIMPIEZA, OCUPADA->DISPONIBLE)
    @PatchMapping("/id-habitacion/{id}/estado/{idEstado}")
    HabitacionResponse cambiarEstado(@PathVariable("id") Long id,
                                     @PathVariable("idEstado") Integer idEstado);
}
