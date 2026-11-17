package com.ramon.reservas.controller;

import com.ramon.commons.dto.reserva.ReservaRequest;
import com.ramon.commons.dto.reserva.ReservaResponse;
import com.ramon.commons.enums.EstadoReserva;
import com.ramon.reservas.services.ReservaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@AllArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    /* ================= CRUD (Notion 6.4) ================= */

    // GET /api/reservas -> USER, ADMIN
    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar() {
        return ResponseEntity.ok(reservaService.listar());
    }

    // GET /api/reservas/{id} -> USER, ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    // POST /api/reservas -> USER, ADMIN
    @PostMapping
    public ResponseEntity<ReservaResponse> registrar(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.registrar(request));
    }

    // PUT /api/reservas/{id} -> USER, ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.ok(reservaService.actualizar(id, request));
    }

    // PATCH /api/reservas/{idReserva}/estado/{idEstado} -> USER, ADMIN
    // 2 = check-in, 3 = check-out, 4 = cancelación
    @PatchMapping("/{idReserva}/estado/{idEstado}")
    public ResponseEntity<ReservaResponse> cambiarEstado(@PathVariable Long idReserva,
                                                         @PathVariable Integer idEstado) {
        return ResponseEntity.ok(reservaService.cambiarEstado(idReserva, idEstado));
    }

    // DELETE /api/reservas/{id} -> solo ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= REPORTES (Notion 7) -> solo ADMIN ================= */

    // GET /api/reservas/reportes/historico
    @GetMapping("/reportes/historico")
    public ResponseEntity<List<ReservaResponse>> reporteHistorico() {
        return ResponseEntity.ok(reservaService.reporteHistorico());
    }

    // GET /api/reservas/reportes/por-estado
    @GetMapping("/reportes/por-estado")
    public ResponseEntity<Map<EstadoReserva, List<ReservaResponse>>> reportePorEstado() {
        return ResponseEntity.ok(reservaService.reportePorEstado());
    }

    /* ================= INTERNOS (Feign). No se mapean en Angular ================= */

    // Para Huéspedes: ¿el huésped tiene reservas CONFIRMADA o EN_CURSO?
    @GetMapping("/id-huesped/{idHuesped}/vigentes")
    public ResponseEntity<Boolean> tieneReservasVigentes(@PathVariable Long idHuesped) {
        return ResponseEntity.ok(reservaService.tieneReservasVigentes(idHuesped));
    }

    // Para Habitaciones: numeroPersonas de la reserva vigente (0 si no hay)
    @GetMapping("/id-habitacion/{idHabitacion}/personas-vigente")
    public ResponseEntity<Integer> personasReservaVigente(@PathVariable Long idHabitacion) {
        return ResponseEntity.ok(reservaService.personasReservaVigente(idHabitacion));
    }
}