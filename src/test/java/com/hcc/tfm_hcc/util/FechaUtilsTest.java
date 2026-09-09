package com.hcc.tfm_hcc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class FechaUtilsTest {

    @Test
    void toIsoDate_conLocalDateTime_devuelveSoloLaFecha() {
        LocalDateTime fecha = LocalDateTime.of(1990, 5, 20, 14, 30);

        assertEquals("1990-05-20", FechaUtils.toIsoDate(fecha));
    }

    @Test
    void toIsoDate_conLocalDate_devuelveLaFechaFormateada() {
        LocalDate fecha = LocalDate.of(1990, 5, 20);

        assertEquals("1990-05-20", FechaUtils.toIsoDate(fecha));
    }

    @Test
    void toIsoDate_conValorNulo_devuelveNull() {
        assertNull(FechaUtils.toIsoDate(null));
    }

    @Test
    void toIsoDate_conOtroTipoDeObjeto_truncaLosPrimeros10Caracteres() {
        assertEquals("1990-05-20", FechaUtils.toIsoDate("1990-05-20T14:30:00"));
    }
}
