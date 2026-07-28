package com.ujeva.util;

/**
 * Utilidades de validación de entrada, pequeñas y sin estado.
 *
 * <p>Sirve como semilla de la suite de pruebas de CI y como ayuda reutilizable para
 * validar copia editable del panel de administración (por ejemplo, evitar textos
 * vacíos o excesivamente largos antes de persistirlos).
 */
public final class Validador {

    private Validador() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Indica si un texto tiene contenido real (no nulo y no solo espacios en blanco).
     *
     * @param texto el texto a evaluar (puede ser {@code null})
     * @return {@code true} si contiene al menos un carácter no en blanco
     */
    public static boolean noVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Indica si un texto no supera un largo máximo.
     *
     * <p>Un texto {@code null} se considera dentro del límite (largo 0).
     *
     * @param texto  el texto a evaluar (puede ser {@code null})
     * @param maximo largo máximo permitido; debe ser {@code >= 0}
     * @return {@code true} si la longitud del texto es menor o igual a {@code maximo}
     * @throws IllegalArgumentException si {@code maximo} es negativo
     */
    public static boolean dentroDeLargo(String texto, int maximo) {
        if (maximo < 0) {
            throw new IllegalArgumentException("El largo máximo no puede ser negativo: " + maximo);
        }
        int largo = (texto == null) ? 0 : texto.length();
        return largo <= maximo;
    }
}
