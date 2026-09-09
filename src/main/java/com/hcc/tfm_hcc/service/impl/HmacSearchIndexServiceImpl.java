package com.hcc.tfm_hcc.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

@Service
public class HmacSearchIndexServiceImpl implements HmacSearchIndexService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Cadena de separación de dominio: garantiza que la clave usada para el índice
     * de búsqueda nunca coincide con la clave usada directamente para cifrar (AES),
     * aunque ambas se deriven en última instancia de la misma clave maestra.
     */
    private static final String INDEX_KEY_CONTEXT = "hcc:search-index:v1";

    private final SecretKeySpec indexKey;

    public HmacSearchIndexServiceImpl(EncryptionKeyProvider encryptionKeyProvider) {
        this.indexKey = new SecretKeySpec(deriveIndexKey(encryptionKeyProvider), HMAC_ALGORITHM);
    }

    @Override
    public String indexar(String valorEnClaro) {
        if (valorEnClaro == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(indexKey);
            byte[] hash = mac.doFinal(valorEnClaro.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error al calcular el índice de búsqueda cifrado.", e);
        }
    }

    private byte[] deriveIndexKey(EncryptionKeyProvider encryptionKeyProvider) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(encryptionKeyProvider.getSecretKey());
            return mac.doFinal(INDEX_KEY_CONTEXT.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error al derivar la clave del índice de búsqueda cifrado.", e);
        }
    }
}
