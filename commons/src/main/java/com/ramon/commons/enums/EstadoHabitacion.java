package com.ramon.commons.enums;

public enum EstadoHabitacion {
    DISPONIBLE(1),
    OCUPADA(2),
    LIMPIEZA(3),
    MANTENIMIENTO(4);

    private final int id;

    EstadoHabitacion(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static EstadoHabitacion fromId(int id) {
        for (EstadoHabitacion estado : values()) {
            if (estado.id == id) {
                return estado;
            }
        }
        return null;
    }
}