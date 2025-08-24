package com.hcc.tfm_hcc.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FechaUtils {
    /**
     * Devuelve la fecha en formato yyyy-MM-dd a partir de un LocalDateTime o LocalDate.
     */
    public static String toIsoDate(Object fecha) {
        if (fecha == null) return null;
        if (fecha instanceof LocalDateTime) {
            return ((LocalDateTime) fecha).toLocalDate().toString();
        }
        if (fecha instanceof LocalDate) {
            return ((LocalDate) fecha).toString();
        }
        return fecha.toString().substring(0, 10);
    }
}
