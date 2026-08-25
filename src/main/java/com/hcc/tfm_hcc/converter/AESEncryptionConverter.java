package com.hcc.tfm_hcc.converter;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertidor de atributos JPA para datos sensibles almacenados en columnas de texto.
 *
 * Cifra con AES/GCM (autenticado, con un vector de inicialización aleatorio en cada
 * llamada), por lo que el mismo valor en claro nunca produce el mismo resultado cifrado
 * dos veces. Los atributos que necesiten seguir siendo consultables por igualdad (NIF,
 * email, tipo de dato clínico...) no deben apoyarse en la igualdad de esta columna:
 * deben usar además un índice de búsqueda determinista aparte, calculado con
 * {@link com.hcc.tfm_hcc.service.HmacSearchIndexService} y almacenado en su propia columna.
 *
 * Compatibilidad con datos ya cifrados por versiones anteriores del proyecto:
 * - Valores con el prefijo "DET1:" fueron cifrados con AES/ECB de forma determinista
 *   (esquema anterior, mantenido solo por compatibilidad de lectura).
 * - Valores sin prefijo se interpretan como AES/GCM (formato actual, y también el que
 *   usaba el esquema "legacy" previo al determinista, indistinguible en formato).
 *
 * Nota: la contraseña no debe usar este convertidor; debe usar solo BCrypt.
 */
@Component
@Converter(autoApply = false)
public class AESEncryptionConverter implements AttributeConverter<String, String> {

    private static final String DETERMINISTIC_PREFIX = "DET1:";
    private static final String DETERMINISTIC_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

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
        return encryptToBase64(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return decryptFromBase64OrPlain(dbData);
    }

    /**
     * Cifra un texto en claro con AES/GCM y lo codifica en Base64, listo para almacenar
     * en una columna de texto. Expuesto (paquete) para que otros conversores que necesiten
     * cifrar tipos distintos de {@code String} (p. ej. {@link AESEncryptionLocalDateTimeConverter})
     * reutilicen la misma clave e implementación en vez de duplicar la lógica criptográfica.
     *
     * @param attribute texto en claro, o {@code null}
     * @return texto cifrado en Base64, o {@code null} si {@code attribute} es {@code null}
     */
    String encryptToBase64(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] ciphertext = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] ivYCiphertext = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, ivYCiphertext, 0, iv.length);
            System.arraycopy(ciphertext, 0, ivYCiphertext, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(ivYCiphertext);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar atributo de datos clínicos.", e);
        }
    }

    /**
     * Descifra un valor de base de datos que puede estar en cualquiera de los tres formatos
     * que ha usado este proyecto (ver la nota de compatibilidad de la clase): legado
     * determinista con prefijo {@code DET1:}, AES/GCM actual, o texto en claro todavía sin
     * migrar (se devuelve tal cual, sin fallar, para permitir migraciones de columna en dos
     * fases). Expuesto (paquete) por el mismo motivo que {@link #encryptToBase64(String)}.
     *
     * @param dbData valor tal como está almacenado en la columna, o {@code null}
     * @return el texto en claro correspondiente, o {@code null} si {@code dbData} es {@code null}
     */
    String decryptFromBase64OrPlain(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            if (dbData.startsWith(DETERMINISTIC_PREFIX)) {
                return decryptDeterministic(dbData.substring(DETERMINISTIC_PREFIX.length()));
            }

            String gcm = tryGcmDecrypt(dbData);
            if (gcm != null) {
                return gcm;
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

    private String tryGcmDecrypt(String dbData) {
        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);

            if (decoded.length < GCM_IV_LENGTH_BYTES + 1) {
                return null;
            }

            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[decoded.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(decoded, 0, iv, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(decoded, GCM_IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception _) {
            return null;
        }
    }
}
