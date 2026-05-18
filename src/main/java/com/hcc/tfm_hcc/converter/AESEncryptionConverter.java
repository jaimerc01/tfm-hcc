package com.hcc.tfm_hcc.converter;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertidor de atributos JPA que cifra datos clínicos sensibles en reposo.
 * 
 * Usa AES/GCM con nonce aleatorio para garantizar confidencialidad.
 * La clave se obtiene de variable de entorno (TFM_HCC_ENCRYPTION_KEY) en tiempo de despliegue.
 * 
 * Seguridad:
 * - La clave debe tener 16, 24 o 32 bytes (AES 128, 192, 256).
 * - Cada cifrado genera un nonce único; se almacena junto al ciphertext.
 * - GCM proporciona autenticidad además de confidencialidad.
 */
@Component
@Converter(autoApply = false)
public class AESEncryptionConverter implements AttributeConverter<String, String> {

    private static final int GCM_IV_LENGTH_BITS = 96;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = GCM_IV_LENGTH_BITS / 8;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKey secretKey;

    /**
     * Constructor que obtiene la clave de variable de entorno.
     * Falla si no existe o tiene longitud inválida.
     * 
     * @param encryptionKeyEnv valor de TFM_HCC_ENCRYPTION_KEY (base64 o hexadecimal de 16/24/32 bytes)
     */
    public AESEncryptionConverter(@Value("${tfm.hcc.encryption.key}") String encryptionKeyEnv) {
        if (encryptionKeyEnv == null || encryptionKeyEnv.trim().isEmpty()) {
            throw new IllegalStateException(
                "Clave de cifrado no configurada. Establece la variable de entorno TFM_HCC_ENCRYPTION_KEY " +
                "con valor en Base64 o hexadecimal (16, 24 o 32 bytes para AES 128/192/256)."
            );
        }
        this.secretKey = parseSecretKey(encryptionKeyEnv.trim());
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // Generar nonce aleatorio para GCM
            byte[] iv = new byte[IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(iv);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            
            byte[] ciphertext = cipher.doFinal(attribute.getBytes());
            
            // Almacenar: [IV || CIPHERTEXT] en Base64 para recuperar el IV en desencriptación
            byte[] ivAndCiphertext = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
            System.arraycopy(ciphertext, 0, ivAndCiphertext, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(ivAndCiphertext);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar atributo de datos clínicos.", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);
            
            // Validar longitud mínima: IV + al menos 1 byte de ciphertext
            if (decoded.length < IV_LENGTH_BYTES + 1) {
                throw new IllegalStateException("Datos cifrados inválidos: longitud insuficiente.");
            }
            
            // Extraer IV y ciphertext
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[decoded.length - IV_LENGTH_BYTES];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(decoded, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext);
        } catch (Exception e) {
            throw new IllegalStateException("Error al descifrar datos clínicos de base de datos.", e);
        }
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
