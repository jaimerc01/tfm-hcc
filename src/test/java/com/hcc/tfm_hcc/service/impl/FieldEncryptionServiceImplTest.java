package com.hcc.tfm_hcc.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;

class FieldEncryptionServiceImplTest {

    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private FieldEncryptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FieldEncryptionServiceImpl(new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64));
    }

    @Test
    void cifrar_conValorNulo_devuelveNulo() {
        assertNull(service.cifrar(null));
    }

    @Test
    void descifrar_conValorNulo_devuelveNulo() {
        assertNull(service.descifrar(null));
    }

    @Test
    void roundTrip_cifrarYDescifrar_devuelveElValorOriginal() {
        String original = "GLUCOSA: 110 mg/dL";

        String cifrado = service.cifrar(original);
        String descifrado = service.descifrar(cifrado);

        assertEquals(original, descifrado);
    }

    @Test
    void cifrar_conElMismoValorDosVeces_produceCifradosDistintos() {
        String cifrado1 = service.cifrar("192.168.1.1");
        String cifrado2 = service.cifrar("192.168.1.1");

        assertNotEquals(cifrado1, cifrado2);
        assertEquals("192.168.1.1", service.descifrar(cifrado1));
        assertEquals("192.168.1.1", service.descifrar(cifrado2));
    }

    @Test
    void descifrar_conValorSinCifrarPrevio_loDevuelveTalCual() {
        String textoPlano = "documento-previo-a-activar-el-cifrado";

        assertEquals(textoPlano, service.descifrar(textoPlano));
    }
}
