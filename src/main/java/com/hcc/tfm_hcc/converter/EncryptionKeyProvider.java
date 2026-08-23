package com.hcc.tfm_hcc.converter;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Proveedor centralizado de la clave AES usada para cifrar datos sensibles.
 *
 * La misma clave se reutiliza tanto para cifrar columnas de base de datos
 * ({@link AESEncryptionConverter}) como el contenido binario de los archivos
 * clínicos almacenados en disco, evitando duplicar el análisis de la clave.
 */
@Component
public class EncryptionKeyProvider {

    private final SecretKey secretKey;

    /**
     * Constructor que obtiene la clave de variable de entorno.
     * Falla si no existe o tiene longitud inválida.
     *
     * @param encryptionKeyEnv valor de TFM_HCC_ENCRYPTION_KEY (base64 o hexadecimal de 16/24/32 bytes)
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
     * Parsea la clave desde representación Base64 o hexadecimal.
     * Soporta longitudes de 16, 24 y 32 bytes (AES 128, 192, 256).
     */
    private SecretKey parseSecretKey(String keyString) {
        byte[] decodedKey;

        try {
            // Intentar Base64 primero
            decodedKey = Base64.getDecoder().decode(keyString);
        } catch (IllegalArgumentException _) {
            try {
                // Intentar hexadecimal
                decodedKey = hexStringToByteArray(keyString);
            } catch (Exception e2) {
                throw new IllegalStateException(
                    "Clave de cifrado inválida. Debe ser Base64 o hexadecimal (16, 24 o 32 bytes).", e2
                );
            }
        }

        // Validar longitud
        if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
            throw new IllegalStateException(
                "Clave de cifrado tiene longitud inválida: " + decodedKey.length + " bytes. " +
                "Debe ser 16 (AES-128), 24 (AES-192) o 32 (AES-256) bytes."
            );
        }

        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    /**
     * Convierte cadena hexadecimal a byte array.
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Cadena hexadecimal debe tener longitud par.");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
