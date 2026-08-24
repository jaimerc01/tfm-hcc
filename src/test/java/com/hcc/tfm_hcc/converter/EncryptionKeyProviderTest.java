package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class EncryptionKeyProviderTest {

    @Test
    void constructor_conClaveBase64De32Bytes_creaClaveAes256() {
        String clave = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

        EncryptionKeyProvider provider = new EncryptionKeyProvider(clave);

        assertEquals("AES", provider.getSecretKey().getAlgorithm());
        assertEquals(32, provider.getSecretKey().getEncoded().length);
    }

    @Test
    void constructor_conClaveBase64De24Bytes_creaClaveAes192() {
        String clave = Base64.getEncoder().encodeToString("012345678901234567890123".getBytes(StandardCharsets.UTF_8));

        EncryptionKeyProvider provider = new EncryptionKeyProvider(clave);

        assertEquals(24, provider.getSecretKey().getEncoded().length);
    }

    @Test
    void constructor_conClaveBase64De16Bytes_creaClaveAes128() {
        String clave = Base64.getEncoder().encodeToString("0123456789012345".getBytes(StandardCharsets.UTF_8));

        EncryptionKeyProvider provider = new EncryptionKeyProvider(clave);

        assertEquals(16, provider.getSecretKey().getEncoded().length);
    }

    @Test
    void constructor_conClaveNula_lanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> new EncryptionKeyProvider(null));
    }

    @Test
    void constructor_conClaveVacia_lanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> new EncryptionKeyProvider("   "));
    }

    @Test
    void constructor_conClaveDeLongitudInvalida_lanzaExcepcion() {
        String claveCorta = Base64.getEncoder().encodeToString("corta".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> new EncryptionKeyProvider(claveCorta));
    }

    @Test
    void constructor_conFormatoInvalido_lanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> new EncryptionKeyProvider("!!!no-es-base64-ni-hex!!!"));
    }
}
