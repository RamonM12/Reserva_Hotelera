package com.ramon.commons.utils;

import com.ramon.commons.exceptions.DatoInvalidoException;

public class StringCustomUtils {

    public static void validarNoVacio(String texto, String mensaje) {
        if (texto == null || texto.isBlank())
            throw new DatoInvalidoException(mensaje);
    }

    public static void validarTamanio(String texto, Integer min, Integer max, String mensaje) {
        validarNoVacio(texto, mensaje);

        if (texto.trim().length() < min || texto.trim().length() > max)
            throw new DatoInvalidoException(mensaje);
    }

    public static String normalizarTexto(String texto) {
        return texto == null ? null : texto.trim();
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static String normalizarTelefono(String telefono) {
        return telefono == null ? null : telefono.replaceAll("\\D", "");
    }
}