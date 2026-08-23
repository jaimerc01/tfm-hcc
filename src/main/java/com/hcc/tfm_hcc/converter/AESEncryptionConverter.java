package com.hcc.tfm_hcc.converter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertidor de atributos JPA para datos sensibles que deben ser consultables por igualdad.
 *
 * Los valores nuevos se almacenan de forma determinista para que el mismo dato siempre produzca
 * el mismo resultado cifrado y pueda seguir usándose en búsquedas y restricciones de unicidad.
 *
 * Compatibilidad:
 * - Los valores antiguos cifrados con AES/GCM siguen siendo legibles.
 * - Los nuevos valores se guardan con un prefijo explícito para distinguir el formato.
 *
 * Nota: la contraseña no debe usar este convertidor; debe usar solo BCrypt.
 */
@Component
@Converter(autoApply = false)
public class AESEncryptionConverter implements AttributeConverter<String, String> {

    private static final String DETERMINISTIC_PREFIX = "DET1:";
    private static final String DETERMINISTIC_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String LEGACY_ALGORITHM = "AES/GCM/NoPadding";
    private static final int LEGACY_IV_LENGTH_BYTES = 12;
    private static final int LEGACY_TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;

    /**
     * Constructor que obtiene la clave a través del proveedor centralizado.
     *
     * @param encryptionKeyProvider proveedor de la clave AES compartida
     */
    public AESEncryptionConverter(EncryptionKeyProvider encryptionKeyProvider) {
        this.secretKey = encryptionKeyProvider.getSecretKey();
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(DETERMINISTIC_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] ciphertext = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return DETERMINISTIC_PREFIX + Base64.getEncoder().encodeToString(ciphertext);
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
            if (dbData.startsWith(DETERMINISTIC_PREFIX)) {
                return decryptDeterministic(dbData.substring(DETERMINISTIC_PREFIX.length()));
            }

            String legacy = tryLegacyDecrypt(dbData);
            if (legacy != null) {
                return legacy;
            }

            return dbData;
        } catch (Exception e) {
            throw new IllegalStateException("Error al descifrar datos clínicos de base de datos.", e);
        }
    }

    private String decryptDeterministic(String encodedCiphertext) throws Exception {
        byte[] ciphertext = Base64.getDecoder().decode(encodedCiphertext);
        Cipher cipher = Cipher.getInstance(DETERMINISTIC_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private String tryLegacyDecrypt(String dbData) {
        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);

            if (decoded.length < LEGACY_IV_LENGTH_BYTES + 1) {
                return null;
            }

            byte[] iv = new byte[LEGACY_IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[decoded.length - LEGACY_IV_LENGTH_BYTES];
            System.arraycopy(decoded, 0, iv, 0, LEGACY_IV_LENGTH_BYTES);
            System.arraycopy(decoded, LEGACY_IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(LEGACY_ALGORITHM);
            javax.crypto.spec.GCMParameterSpec spec = new javax.crypto.spec.GCMParameterSpec(LEGACY_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception _) {
            return null;
        }
    }

}
