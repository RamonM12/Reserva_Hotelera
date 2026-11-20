package com.edwin.habitaciones.entity;

import com.ramon.commons.enums.EstadoHabitacion;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.TipoHabitacion;
import com.ramon.commons.exceptions.ConflictException;
import com.ramon.commons.exceptions.DatoInvalidoException;
import com.ramon.commons.utils.ValoresNumericosUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "HABITACIONES")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HABITACION")
    private Long id;

    @Column(name = "NUMERO", nullable = false)
    private Long numero;

    @Column(name = "PRECIO", nullable = false)
    private BigDecimal precio;

    @Column(name = "CAPACIDAD", nullable = false)
    private Long capacidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false)
    private EstadoHabitacion estadoHabitacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", nullable = false)
    private EstadoRegistro estadoRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false)
    private TipoHabitacion tipoHabitacion;

    private void validarNoEliminado() {
        if (this.estadoRegistro == EstadoRegistro.ELIMINADO) {
            throw new ConflictException("La habitación ya se encuentra eliminada.");
        }
    }

    public static void validarDatos(Long numero, BigDecimal precio, Long capacidad, TipoHabitacion tipoHabitacion) {
        ValoresNumericosUtils.validarLongPositivo(numero, "El número de habitación debe ser un número entero y mayor que 0.");
        ValoresNumericosUtils.validarBigDecimalPositivo(precio, "El precio de la habitación debe ser mayor a 0.");
        ValoresNumericosUtils.validarLongPositivo(capacidad, "La capacidad de la habitación debe ser al menos 1.");

        if (tipoHabitacion == null) {
            throw new DatoInvalidoException("El tipo de habitación es obligatorio."); // 400, antes daba 500
        }
    }

    public void actualizar(Long numero, BigDecimal precio, Long capacidad, TipoHabitacion tipoHabitacion) {
        validarNoEliminado();
        validarDatos(numero, precio, capacidad, tipoHabitacion);

        this.numero = numero;
        this.precio = precio;
        this.capacidad = capacidad;
        this.tipoHabitacion = tipoHabitacion;
    }

    public void eliminar() {
        validarNoEliminado();
        if (this.estadoHabitacion == EstadoHabitacion.OCUPADA) {
            throw new ConflictException("No se puede eliminar una habitación OCUPADA."); // Notion 6.3
        }
        this.estadoRegistro = EstadoRegistro.ELIMINADO;
    }

    public void cambiarEstado(EstadoHabitacion nuevoEstado) {
        this.estadoHabitacion = nuevoEstado;
    }

    public static Habitacion crear(Long numero, BigDecimal precio, Long capacidad, TipoHabitacion tipoHabitacion) {
        validarDatos(numero, precio, capacidad, tipoHabitacion);

        return Habitacion.builder()
                .numero(numero)
                .precio(precio)
                .capacidad(capacidad)
                .tipoHabitacion(tipoHabitacion)
                .estadoHabitacion(EstadoHabitacion.DISPONIBLE)
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();
    }
}