package com.ramon.huespedes.controller;


import com.ramon.commons.dto.huesped.HuespedRequest;
import com.ramon.commons.dto.huesped.HuespedResponse;
import com.ramon.huespedes.service.HuespedService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/huespedes")
@AllArgsConstructor
public class HuespedController {

    private final HuespedService huespedService;

    @GetMapping
    public ResponseEntity<List<HuespedResponse>> listar() {
        return ResponseEntity.ok(huespedService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HuespedResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(huespedService.buscarPorId(id));
    }

    @GetMapping("/id-huesped/{id}")
    public ResponseEntity<HuespedResponse> buscarInterno(@PathVariable Long id) {
        return ResponseEntity.ok(huespedService.buscarInterno(id));
    }

    @PostMapping
    public ResponseEntity<HuespedResponse> registrar(@Valid @RequestBody HuespedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(huespedService.registrar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HuespedResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody HuespedRequest request) {
        return ResponseEntity.ok(huespedService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        huespedService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}