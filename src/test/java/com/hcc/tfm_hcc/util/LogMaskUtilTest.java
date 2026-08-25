package com.hcc.tfm_hcc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LogMaskUtilTest {

    @Test
    void enmascarar_conNifNormal_dejaVisiblesLosUltimosTresCaracteres() {
        assertEquals("***78A", LogMaskUtil.enmascarar("12345678A"));
    }

    @Test
    void enmascarar_conValorNulo_devuelveLaCadenaNull() {
        assertEquals("null", LogMaskUtil.enmascarar(null));
    }

    @Test
    void enmascarar_conCadenaVacia_devuelveMascaraCompleta() {
        assertEquals("***", LogMaskUtil.enmascarar(""));
    }

    @Test
    void enmascarar_conCadenaEnBlanco_devuelveMascaraCompleta() {
        assertEquals("***", LogMaskUtil.enmascarar("   "));
    }

    @Test
    void enmascarar_conValorMasCortoQueLaParteVisible_devuelveMascaraCompleta() {
        assertEquals("***", LogMaskUtil.enmascarar("AB"));
    }

    @Test
    void enmascarar_conValorDeLaMismaLongitudQueLaParteVisible_devuelveMascaraCompleta() {
        assertEquals("***", LogMaskUtil.enmascarar("ABC"));
    }

    @Test
    void enmascarar_conValorUnCaracterMasLargoQueLaParteVisible_dejaVisiblesLosUltimosTresCaracteres() {
        assertEquals("***BCD", LogMaskUtil.enmascarar("ABCD"));
    }
}
