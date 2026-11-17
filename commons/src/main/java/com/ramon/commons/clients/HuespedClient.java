package com.ramon.commons.clients;

import com.ramon.commons.dto.huesped.HuespedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "huespedes" = spring.application.name del microservicio (lo encuentra por Eureka)
@FeignClient(name = "huespedes", path = "/api/huespedes")
public interface HuespedClient {

    // Endpoint interno: devuelve el huésped aunque esté ELIMINADO (Notion 6.2)
    @GetMapping("/id-huesped/{id}")
    HuespedResponse buscarPorId(@PathVariable("id") Long id);
}
