package com.hcc.tfm_hcc.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utilidades de formato de fechas. Clase de utilidad: no instanciable.
 */
public final class FechaUtils {

    private FechaUtils() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    /**
     * Devuelve la fecha en formato yyyy-MM-dd a partir de un LocalDateTime o LocalDate.
     */
    public static String toIsoDate(Object fecha) {
        if (fecha == null) {
            return null;
        }
        if (fecha instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate().toString();
        }
        if (fecha instanceof LocalDate localDate) {
            return localDate.toString();
        }
        String texto = fecha.toString();
        return texto.length() >= 10 ? texto.substring(0, 10) : texto;
    }
}
