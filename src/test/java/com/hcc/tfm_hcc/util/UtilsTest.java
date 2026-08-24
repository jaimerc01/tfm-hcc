package com.hcc.tfm_hcc.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void obtenerHoraActual_devuelveUnaFechaCercanaAAhora() {
        long antes = System.currentTimeMillis();

        Date resultado = Utils.obtenerHoraActual();

        long despues = System.currentTimeMillis();
        assertNotNull(resultado);
        assertTrue(resultado.getTime() >= antes && resultado.getTime() <= despues);
    }
}
