package com.hcc.tfm_hcc.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;

class TotpServiceImplTest {

    private static final int PASO_SEGUNDOS = 30;

    private final TotpServiceImpl service = new TotpServiceImpl();

    @Test
    void generarSecreto_devuelveUnSecretoBase32SinRelleno() {
        String secreto = service.generarSecreto();

        assertNotNull(secreto);
        assertFalse(secreto.contains("="));
        assertTrue(secreto.matches("[A-Z2-7]+"));
        // No debe lanzar excepción: confirma que es un Base32 válido y decodificable
        assertEquals(20, new Base32().decode(secreto).length);
    }

    @Test
    void generarSecreto_generaValoresDistintosCadaVez() {
        String secreto1 = service.generarSecreto();
        String secreto2 = service.generarSecreto();

        assertFalse(secreto1.equals(secreto2));
    }

    @Test
    void generarOtpAuthUri_incluyeElSecretoYLosParametrosEsperados() {
        String uri = service.generarOtpAuthUri("ABCDEFGH", "12345678A");

        assertTrue(uri.startsWith("otpauth://totp/"));
        assertTrue(uri.contains("secret=ABCDEFGH"));
        assertTrue(uri.contains("algorithm=SHA1"));
        assertTrue(uri.contains("digits=6"));
        assertTrue(uri.contains("period=30"));
        assertTrue(uri.contains("12345678A"));
    }

    @Test
    void validarCodigo_conElCodigoActualCorrecto_esValido() {
        String secreto = service.generarSecreto();

        String codigoActual = calcularCodigoDeReferencia(secreto, pasoActual());

        assertTrue(service.validarCodigo(secreto, codigoActual));
    }

    @Test
    void validarCodigo_conUnCodigoIncorrecto_noEsValido() {
        String secreto = service.generarSecreto();

        String codigoValido = calcularCodigoDeReferencia(secreto, pasoActual());
        String codigoInvalido = codigoValido.equals("000000") ? "111111" : "000000";

        assertFalse(service.validarCodigo(secreto, codigoInvalido));
    }

    @Test
    void validarCodigo_toleraElPasoAnteriorYElSiguiente() {
        String secreto = service.generarSecreto();
        long paso = pasoActual();

        assertTrue(service.validarCodigo(secreto, calcularCodigoDeReferencia(secreto, paso - 1)));
        assertTrue(service.validarCodigo(secreto, calcularCodigoDeReferencia(secreto, paso + 1)));
    }

    @Test
    void validarCodigo_fueraDeLaVentanaDeTolerancia_noEsValido() {
        String secreto = service.generarSecreto();
        long paso = pasoActual();

        assertFalse(service.validarCodigo(secreto, calcularCodigoDeReferencia(secreto, paso + 2)));
        assertFalse(service.validarCodigo(secreto, calcularCodigoDeReferencia(secreto, paso - 2)));
    }

    @Test
    void validarCodigo_conSecretoNulo_noEsValido() {
        assertFalse(service.validarCodigo(null, "123456"));
    }

    @Test
    void validarCodigo_conCodigoNulo_noEsValido() {
        assertFalse(service.validarCodigo(service.generarSecreto(), null));
    }

    @Test
    void validarCodigo_conCodigoConLetras_noEsValido() {
        assertFalse(service.validarCodigo(service.generarSecreto(), "12345a"));
    }

    @Test
    void validarCodigo_conCodigoDeLongitudIncorrecta_noEsValido() {
        assertFalse(service.validarCodigo(service.generarSecreto(), "12345"));
        assertFalse(service.validarCodigo(service.generarSecreto(), "1234567"));
    }

    private long pasoActual() {
        return Instant.now().getEpochSecond() / PASO_SEGUNDOS;
    }

    /**
     * Reimplementación independiente de HOTP/TOTP (RFC 4226/6238), usada solo para
     * calcular en el test el código esperado para un paso de tiempo concreto y así
     * comprobar el resultado de {@link TotpServiceImpl#validarCodigo} sin depender
     * de temporizaciones reales ni de acceder a métodos privados de la clase.
     */
    private String calcularCodigoDeReferencia(String secretoBase32, long paso) {
        try {
            byte[] clave = new Base32().decode(secretoBase32);
            byte[] contador = ByteBuffer.allocate(8).putLong(paso).array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(clave, "HmacSHA1"));
            byte[] hash = mac.doFinal(contador);

            int offset = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int codigo = binario % 1_000_000;
            return String.format("%06d", codigo);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
