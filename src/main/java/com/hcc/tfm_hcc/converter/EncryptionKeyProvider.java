package com.hcc.tfm_hcc.converter;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Proveedor centralizado de la clave AES usada para cifrar datos sensibles.
 *
 * La misma clave se reutiliza tanto para cifrar columnas de base de datos
 * ({@link AESEncryptionConverter}) como el contenido binario de los archivos
 * clínicos almacenados en disco, evitando duplicar el análisis de la clave.
 *
 * <p>La clave ({@code TFM_HCC_ENCRYPTION_KEY}) se acepta en Base64 o en hexadecimal y
 * debe decodificar a 16, 24 o 32 bytes (AES-128/192/256). Se prueba primero Base64 y, si
 * no produce una longitud válida, se prueba hexadecimal, de modo que una clave hex de 64
 * caracteres (que además es Base64 válido pero decodifica a 48 bytes) se interprete
 * correctamente.</p>
 */
@Slf4j
@Component
public class EncryptionKeyProvider {

    private static final String MENSAJE_FORMATO_INVALIDO =
            "Clave de cifrado inválida. Debe ser Base64 o hexadecimal que decodifique a 16, 24 o 32 bytes "
            + "(AES-128/192/256).";

    private final SecretKey secretKey;

    /**
     * Constructor que obtiene la clave de variable de entorno.
     * Falla si no existe o tiene longitud inválida.
     *
     * @param encryptionKeyEnv valor de TFM_HCC_ENCRYPTION_KEY (Base64 o hexadecimal de 16/24/32 bytes)
     */
    public EncryptionKeyProvider(@Value("${tfm.hcc.encryption.key}") String encryptionKeyEnv) {
        if (encryptionKeyEnv == null || encryptionKeyEnv.trim().isEmpty()) {
            throw new IllegalStateException(
                "Clave de cifrado no configurada. Establece la variable de entorno TFM_HCC_ENCRYPTION_KEY " +
                "con valor en Base64 o hexadecimal (16, 24 o 32 bytes para AES 128/192/256)."
            );
        }
        this.secretKey = parseSecretKey(encryptionKeyEnv.trim());
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * Parsea la clave desde representación Base64 o hexadecimal, exigiendo que decodifique
     * a 16, 24 o 32 bytes.
     */
    private SecretKey parseSecretKey(String keyString) {
        byte[] desdeBase64 = intentarBase64(keyString);
        if (esLongitudValida(desdeBase64)) {
            avisarSiAmbigua(keyString);
            return new SecretKeySpec(desdeBase64, "AES");
        }

        byte[] desdeHex = intentarHex(keyString);
        if (esLongitudValida(desdeHex)) {
            return new SecretKeySpec(desdeHex, "AES");
        }

        // Ninguna interpretación produce una longitud de clave AES válida.
        int longitudBase64 = desdeBase64 != null ? desdeBase64.length : -1;
        throw new IllegalStateException(longitudBase64 >= 0
                ? MENSAJE_FORMATO_INVALIDO + " (Base64 decodifica a " + longitudBase64 + " bytes)"
                : MENSAJE_FORMATO_INVALIDO);
    }

    private boolean esLongitudValida(byte[] clave) {
        return clave != null && (clave.length == 16 || clave.length == 24 || clave.length == 32);
    }

    private byte[] intentarBase64(String keyString) {
        try {
            return Base64.getDecoder().decode(keyString);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    private byte[] intentarHex(String keyString) {
        if (keyString.length() % 2 != 0 || !keyString.matches("[0-9a-fA-F]+")) {
            return null;
        }
        int len = keyString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(keyString.charAt(i), 16) << 4)
                    + Character.digit(keyString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Una cadena de 32 dígitos hexadecimales también es Base64 válido y decodifica a 24
     * bytes: es imposible saber si el operador quería hex (16 bytes) o Base64 (24 bytes).
     * Se interpreta como Base64 (comportamiento histórico) pero se avisa para que use un
     * formato inequívoco: Base64 explícito, o hex de 64 caracteres para AES-256.
     */
    private void avisarSiAmbigua(String keyString) {
        if (keyString.matches("[0-9a-fA-F]{32}")) {
            log.warn("La clave de cifrado son 32 dígitos hexadecimales: es ambigua (Base64 -> 24 bytes, "
                    + "hex -> 16 bytes). Se está interpretando como Base64 (AES-192). Usa Base64 explícito "
                    + "o 64 dígitos hex para evitar la ambigüedad.");
        }
    }
}
