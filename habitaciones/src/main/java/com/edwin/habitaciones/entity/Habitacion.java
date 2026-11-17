package com.edwin.habitaciones.entity;

import com.ramon.commons.enums.EstadoHabitacion;
import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.TipoHabitacion;
import com.ramon.commons.exceptions.ConflictException;
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

    @Column(name = "NUMERO", nullable = false, unique = true)
    private Long numero;

    @Column(name = "PRECIO", nullable = false)
    private BigDecimal precio;

    @Column(name = "CAPACIDAD", nullable = false)
    private Long capacidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_HABITACION", nullable = false)
    private EstadoHabitacion estadoHabitacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", nullable = false)
    private EstadoRegistro estadoRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_HABITACION", nullable = false)
    private TipoHabitacion tipoHabitacion;

    // ==========================================
    // MÉTODOS DE NEGOCIO Y VALIDACIONES
    // ==========================================

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
            throw new IllegalArgumentException("El tipo de habitación es obligatorio.");
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
        this.estadoRegistro = EstadoRegistro.ELIMINADO;
    }

    public static Habitacion crear(Long numero, BigDecimal precio, Long capacidad, TipoHabitacion tipoHabitacion) {
        validarDatos(numero, precio, capacidad, tipoHabitacion);

        return Habitacion.builder()
                .numero(numero)
                .precio(precio)
                .capacidad(capacidad)
                .tipoHabitacion(tipoHabitacion)
                .estadoHabitacion(EstadoHabitacion.DISPONIBLE) // Estado inicial según requerimiento
                .estadoRegistro(EstadoRegistro.ACTIVO)       // Estado de registro inicial
                .build();
    }
}