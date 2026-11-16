package com.ramon.huespedes.service;

import com.ramon.commons.dto.huesped.HuespedRequest;
import com.ramon.commons.dto.huesped.HuespedResponse;

import java.util.List;

public interface HuespedService {

    List<HuespedResponse> listar();

    HuespedResponse buscarPorId(Long id);

    HuespedResponse buscarInterno(Long id); // para Reservas: sin importar el estadoRegistro

    HuespedResponse registrar(HuespedRequest request);

    HuespedResponse actualizar(Long id, HuespedRequest request);

    void eliminar(Long id);
}
