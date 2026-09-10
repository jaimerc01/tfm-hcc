package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AESEncryptionConverterTest {

    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private AESEncryptionConverter converter;
    private EncryptionKeyProvider keyProvider;

    @BeforeEach
    void setUp() {
        keyProvider = new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64);
        converter = new AESEncryptionConverter(keyProvider);
    }

    @Test
    void convertToDatabaseColumn_conValorNulo_devuelveNulo() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_conValorNulo_devuelveNulo() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToDatabaseColumn_devuelveBase64ConMasBytesQueElIv() {
        String cifrado = converter.convertToDatabaseColumn("12345678A");

        assertNotNull(cifrado);
        assertTrue(Base64.getDecoder().decode(cifrado).length > 12);
    }

    @Test
    void roundTrip_cifrarYDescifrar_devuelveElValorOriginal() {
        String original = "usuario@example.com";

        String cifrado = converter.convertToDatabaseColumn(original);
        String descifrado = converter.convertToEntityAttribute(cifrado);

        assertEquals(original, descifrado);
    }

    @Test
    void convertToDatabaseColumn_noEsDeterminista_mismoValorProduceCifradosDistintos() {
        String cifrado1 = converter.convertToDatabaseColumn("12345678A");
        String cifrado2 = converter.convertToDatabaseColumn("12345678A");

        assertTrue(!cifrado1.equals(cifrado2));
        assertEquals("12345678A", converter.convertToEntityAttribute(cifrado1));
        assertEquals("12345678A", converter.convertToEntityAttribute(cifrado2));
    }

    @Test
    void convertToEntityAttribute_conCriptogramaGcmValido_loDescifra() throws Exception {
        String original = "dato-clinico";
        String criptograma = cifrarGcm(original);

        assertEquals(original, converter.convertToEntityAttribute(criptograma));
    }

    @Test
    void convertToEntityAttribute_conValorNoDescifrable_lanzaExcepcion() {
        assertThrows(IllegalStateException.class,
                () -> converter.convertToEntityAttribute("texto-plano-cualquiera"));
    }

    private String cifrarGcm(String plaintext) throws Exception {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getSecretKey(), spec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combinado = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combinado, 0, iv.length);
        System.arraycopy(ciphertext, 0, combinado, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combinado);
    }
}
