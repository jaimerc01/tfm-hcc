package com.hcc.tfm_hcc.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;
import com.hcc.tfm_hcc.service.ArchivoCifradoService;

import lombok.RequiredArgsConstructor;

/**
 * Cifra y descifra el contenido binario de los archivos clínicos con AES/GCM,
 * reutilizando la misma clave configurada para el cifrado en reposo de las
 * columnas de base de datos (ver {@link EncryptionKeyProvider}).
 *
 * <p>Formato en disco: los primeros 12 bytes del fichero son el vector de
 * inicialización (IV) en claro, generado aleatoriamente para cada archivo;
 * el resto del fichero es el contenido cifrado junto con la etiqueta de
 * autenticación GCM. El IV no necesita mantenerse en secreto, solo ser
 * distinto en cada archivo para que el cifrado sea seguro.</p>
 */
@Service
@RequiredArgsConstructor
public class ArchivoCifradoServiceImpl implements ArchivoCifradoService {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final EncryptionKeyProvider encryptionKeyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void cifrar(InputStream entrada, OutputStream salida) throws IOException {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            salida.write(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKeyProvider.getSecretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            try (CipherOutputStream cipherOut = new CipherOutputStream(salida, cipher)) {
                entrada.transferTo(cipherOut);
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(ErrorMessages.ERROR_GUARDAR_ARCHIVO, e);
        }
    }

    @Override
    public InputStream descifrar(InputStream entradaCifrada) throws IOException {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            int leidos = entradaCifrada.readNBytes(iv, 0, IV_LENGTH_BYTES);
            if (leidos != IV_LENGTH_BYTES) {
                throw new IOException(ErrorMessages.ERROR_ARCHIVO_NO_ACCESIBLE);
            }

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKeyProvider.getSecretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            return new CipherInputStream(entradaCifrada, cipher);
        } catch (GeneralSecurityException e) {
            entradaCifrada.close();
            throw new IOException(ErrorMessages.ERROR_ARCHIVO_NO_ACCESIBLE, e);
        } catch (IOException e) {
            entradaCifrada.close();
            throw e;
        }
    }
}
