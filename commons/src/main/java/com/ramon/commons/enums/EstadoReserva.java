package com.ramon.commons.enums;

public enum EstadoReserva {
    CONFIRMADA(1),
    EN_CURSO(2),
    FINALIZADA(3),
    CANCELADA(4);

    private final int id;

    EstadoReserva(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static EstadoReserva fromId(int id) {
        for (EstadoReserva estado : values()) {
            if (estado.id == id) {
                return estado;
            }
        }
        return null;
    }
}