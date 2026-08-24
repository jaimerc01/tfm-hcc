package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;
import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;

class LegacyAwarePasswordEncoderTest {

    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private LegacyAwarePasswordEncoder encoder;
    private AESEncryptionConverter encryptionConverter;

    @BeforeEach
    void setUp() {
        encryptionConverter = new AESEncryptionConverter(new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64));
        encoder = new LegacyAwarePasswordEncoder(encryptionConverter);
    }

    @Test
    void encode_devuelveUnHashBcrypt() {
        String hash = encoder.encode("miContrasena123");

        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    @Test
    void matches_conHashBcryptDirecto_devuelveTrue() {
        String hash = encoder.encode("miContrasena123");

        assertTrue(encoder.matches("miContrasena123", hash));
    }

    @Test
    void matches_conHashBcryptDirectoYContrasenaIncorrecta_devuelveFalse() {
        String hash = encoder.encode("miContrasena123");

        assertFalse(encoder.matches("otraContrasena", hash));
    }

    @Test
    void matches_conHashBcryptCifradoComoLegado_devuelveTrue() {
        String hashBcrypt = encoder.encode("miContrasena123");
        String hashCifradoComoLegado = encryptionConverter.convertToDatabaseColumn(hashBcrypt);

        assertTrue(encoder.matches("miContrasena123", hashCifradoComoLegado));
    }

    @Test
    void matches_conRawPasswordNulo_devuelveFalse() {
        assertFalse(encoder.matches(null, encoder.encode("x")));
    }

    @Test
    void matches_conEncodedPasswordNulo_devuelveFalse() {
        assertFalse(encoder.matches("x", null));
    }

    @Test
    void matches_conEncodedPasswordVacio_devuelveFalse() {
        assertFalse(encoder.matches("x", "   "));
    }

    @Test
    void matches_conValorNoDescifrableNiBcrypt_devuelveFalse() {
        assertFalse(encoder.matches("cualquiera", "valor-sin-relacion-alguna"));
    }
}
