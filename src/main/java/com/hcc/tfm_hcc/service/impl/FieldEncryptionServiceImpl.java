package com.hcc.tfm_hcc.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;
import com.hcc.tfm_hcc.service.FieldEncryptionService;

/**
 * Implementación AES/GCM de {@link FieldEncryptionService}.
 *
 * <p>Deliberadamente reimplementa (en vez de reutilizar) el mismo esquema AES/GCM
 * que ya usa {@code AESEncryptionConverter} para columnas JPA: ese converter está
 * ligado a la interfaz {@code AttributeConverter} de JPA y acoplado a la
 * compatibilidad con formatos heredados (AES/ECB determinista), que no aplican
 * aquí. Reutilizar directamente esa clase habría exigido tocar su constructor y,
 * con ello, cada test que la instancia manualmente, por un beneficio marginal.
 * Ambas clases sí comparten la misma clave a través de {@link EncryptionKeyProvider}.
 */
@Service
public class FieldEncryptionServiceImpl implements FieldEncryptionService {

    private static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public FieldEncryptionServiceImpl(EncryptionKeyProvider encryptionKeyProvider) {
        this.secretKey = encryptionKeyProvider.getSecretKey();
    }

    @Override
    public String cifrar(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] ciphertext = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            byte[] ivYCiphertext = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, ivYCiphertext, 0, iv.length);
            System.arraycopy(ciphertext, 0, ivYCiphertext, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(ivYCiphertext);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar el campo", e);
        }
    }

    @Override
    public String descifrar(String valorAlmacenado) {
        if (valorAlmacenado == null) {
            return null;
        }
        String descifrado = tryGcmDecrypt(valorAlmacenado);
        return descifrado != null ? descifrado : valorAlmacenado;
    }

    private String tryGcmDecrypt(String valor) {
        try {
            byte[] decoded = Base64.getDecoder().decode(valor);

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
