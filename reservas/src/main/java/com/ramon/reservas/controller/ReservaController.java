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


    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar() {
        return ResponseEntity.ok(reservaService.listar());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }


    @PostMapping
    public ResponseEntity<ReservaResponse> registrar(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.registrar(request));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.ok(reservaService.actualizar(id, request));
    }


    @PatchMapping("/{idReserva}/estado/{idEstado}")
    public ResponseEntity<ReservaResponse> cambiarEstado(@PathVariable Long idReserva,
                                                         @PathVariable Integer idEstado) {
        return ResponseEntity.ok(reservaService.cambiarEstado(idReserva, idEstado));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/reportes/historico")
    public ResponseEntity<List<ReservaResponse>> reporteHistorico() {
        return ResponseEntity.ok(reservaService.reporteHistorico());
    }

    @GetMapping("/reportes/por-estado")
    public ResponseEntity<Map<EstadoReserva, List<ReservaResponse>>> reportePorEstado() {
        return ResponseEntity.ok(reservaService.reportePorEstado());
    }


    @GetMapping("/id-huesped/{idHuesped}/vigentes")
    public ResponseEntity<Boolean> tieneReservasVigentes(@PathVariable Long idHuesped) {
        return ResponseEntity.ok(reservaService.tieneReservasVigentes(idHuesped));
    }

    @GetMapping("/id-habitacion/{idHabitacion}/personas-vigente")
    public ResponseEntity<Integer> personasReservaVigente(@PathVariable Long idHabitacion) {
        return ResponseEntity.ok(reservaService.personasReservaVigente(idHabitacion));
    }
}