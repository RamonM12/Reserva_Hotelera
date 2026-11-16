package com.ramon.huespedes.entity;

import com.ramon.commons.enums.EstadoRegistro;
import com.ramon.commons.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "HUESPEDES")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HUESPED")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 50)
    private String apellido;

    @Column(name = "EMAIL", nullable = false, length = 254)
    private String email;

    @Column(name = "TELEFONO", nullable = false, length = 10)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_DOCUMENTO", nullable = false, length = 9)
    private TipoDocumento tipoDocumento;

    @Column(name = "NUMERO_DOCUMENTO", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "NACIONALIDAD", nullable = false, length = 50)
    private String nacionalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", nullable = false, length = 9)
    private EstadoRegistro estadoRegistro;
}