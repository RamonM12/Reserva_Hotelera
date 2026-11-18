package com.ramon.commons.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reservas", path = "/api/reservas")
public interface ReservaClient {

    // Para Habitaciones: numeroPersonas de la reserva vigente (0 si no hay)
    @GetMapping("/id-habitacion/{idHabitacion}/personas-vigente")
    Integer personasReservaVigente(@PathVariable("idHabitacion") Long idHabitacion);

    // Para Huéspedes: ¿tiene reservas CONFIRMADA o EN_CURSO?
    @GetMapping("/id-huesped/{idHuesped}/vigentes")
    Boolean tieneReservasVigentes(@PathVariable("idHuesped") Long idHuesped);
}