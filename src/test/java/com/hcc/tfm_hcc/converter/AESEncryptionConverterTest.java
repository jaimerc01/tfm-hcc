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
    void convertToDatabaseColumn_noUsaElPrefijoDeterminista() {
        String cifrado = converter.convertToDatabaseColumn("12345678A");

        assertNotNull(cifrado);
        assertTrue(!cifrado.startsWith("DET1:"));
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
    void convertToEntityAttribute_conFormatoDeterministaAntiguo_sigueSiendoLegible() throws Exception {
        String original = "12345678A";
        String ciphertextDeterminista = "DET1:" + cifrarConFormatoDeterminista(original);

        String descifrado = converter.convertToEntityAttribute(ciphertextDeterminista);

        assertEquals(original, descifrado);
    }

    @Test
    void convertToEntityAttribute_conFormatoLegacyGcm_sigueSiendoLegible() throws Exception {
        String original = "dato-clinico-legacy";
        String legacyCiphertext = cifrarConFormatoLegacyGcm(original);

        String descifrado = converter.convertToEntityAttribute(legacyCiphertext);

        assertEquals(original, descifrado);
    }

    @Test
    void convertToEntityAttribute_conValorNoReconocidoNiPrefijadoNiLegacy_devuelveElValorSinCambios() {
        String valorSinCifrar = "texto-plano-de-migracion";

        String resultado = converter.convertToEntityAttribute(valorSinCifrar);

        assertEquals(valorSinCifrar, resultado);
    }

    @Test
    void convertToEntityAttribute_conPrefijoDeterministaPeroContenidoCorrupto_lanzaExcepcion() {
        String corrupto = "DET1:no-es-base64-valido!!!";

        assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(corrupto));
    }

    private String cifrarConFormatoDeterminista(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getSecretKey());

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ciphertext);
    }

    private String cifrarConFormatoLegacyGcm(String plaintext) throws Exception {
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
