package com.ramon.reservas.entity;

import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "RESERVAS")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESERVA")
    private Long id;

    // Sin @ManyToOne: el huésped vive en otro microservicio, se consulta por Feign
    @Column(name = "ID_HUESPED", nullable = false)
    private Long idHuesped;

    // Igual: la habitación vive en otro microservicio
    @Column(name = "ID_HABITACION", nullable = false)
    private Long idHabitacion;

    @Column(name = "FECHA_ENTRADA", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "FECHA_SALIDA", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "NUMERO_PERSONAS", nullable = false)
    private Integer numeroPersonas;

    // Copia del precio de la habitación al crear (Notion 4.3)
    @Column(name = "PRECIO_NOCHE", nullable = false)
    private BigDecimal precioNoche;

    // noches x precioNoche, lo calcula el backend (Notion 4.3)
    @Column(name = "PRECIO_TOTAL", nullable = false)
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 10)
    private EstadoReserva estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", nullable = false, length = 9)
    private EstadoRegistro estadoRegistro;
}