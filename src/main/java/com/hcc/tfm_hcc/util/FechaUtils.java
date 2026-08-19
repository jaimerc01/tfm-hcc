package com.hcc.tfm_hcc.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FechaUtils {

    private FechaUtils() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Devuelve la fecha en formato yyyy-MM-dd a partir de un LocalDateTime o LocalDate.
     */
    public static String toIsoDate(Object fecha) {
        if (fecha == null) return null;
        if (fecha instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate().toString();
        }
        if (fecha instanceof LocalDate localDate) {
            return localDate.toString();
        }
        return fecha.toString().substring(0, 10);
    }
}
