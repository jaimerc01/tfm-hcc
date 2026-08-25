package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.converter.EncryptionKeyProvider;
import com.hcc.tfm_hcc.service.impl.ArchivoCifradoServiceImpl;

class ArchivoCifradoServiceImplTest {

    // Clave AES-256 de pruebas, sin relación con la clave real de ningún entorno.
    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private ArchivoCifradoServiceImpl service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        EncryptionKeyProvider keyProvider = new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64);
        service = new ArchivoCifradoServiceImpl(keyProvider);
    }

    @Test
    void cifrarYDescifrar_devuelveElContenidoOriginal() throws IOException {
        byte[] contenidoOriginal = "Informe clínico confidencial del paciente".getBytes(StandardCharsets.UTF_8);
        Path archivoCifrado = tempDir.resolve("archivo.enc");

        try (var entrada = new ByteArrayInputStream(contenidoOriginal);
             var salida = Files.newOutputStream(archivoCifrado)) {
            service.cifrar(entrada, salida);
        }

        byte[] contenidoDescifrado;
        try (var descifrado = service.descifrar(Files.newInputStream(archivoCifrado))) {
            contenidoDescifrado = descifrado.readAllBytes();
        }

        assertArrayEquals(contenidoOriginal, contenidoDescifrado);
    }

    @Test
    void cifrar_noAlmacenaElContenidoEnClaroEnDisco() throws IOException {
        byte[] contenidoOriginal = "dato-clinico-sensible-12345".getBytes(StandardCharsets.UTF_8);
        Path archivoCifrado = tempDir.resolve("archivo.enc");

        try (var entrada = new ByteArrayInputStream(contenidoOriginal);
             var salida = Files.newOutputStream(archivoCifrado)) {
            service.cifrar(entrada, salida);
        }

        byte[] contenidoEnDisco = Files.readAllBytes(archivoCifrado);
        String textoPlano = new String(contenidoEnDisco, StandardCharsets.UTF_8);

        assertFalse(textoPlano.contains("dato-clinico-sensible-12345"));
    }

    @Test
    void cifrar_produceResultadosDistintosParaElMismoContenido() throws IOException {
        byte[] contenidoOriginal = "mismo contenido dos veces".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream salida1 = new ByteArrayOutputStream();
        ByteArrayOutputStream salida2 = new ByteArrayOutputStream();

        service.cifrar(new ByteArrayInputStream(contenidoOriginal), salida1);
        service.cifrar(new ByteArrayInputStream(contenidoOriginal), salida2);

        assertFalse(Arrays.equals(salida1.toByteArray(), salida2.toByteArray()));
    }

    @Test
    void descifrar_lanzaExcepcionSiElArchivoEstaCorrupto() throws IOException {
        Path archivoCorrupto = tempDir.resolve("corrupto.enc");
        Files.write(archivoCorrupto, "contenido-no-cifrado-demasiado-corto".getBytes(StandardCharsets.UTF_8));

        assertThrows(IOException.class, () -> {
            try (var descifrado = service.descifrar(Files.newInputStream(archivoCorrupto))) {
                descifrado.readAllBytes();
            }
        });
    }

    @Test
    void descifrar_lanzaExcepcionSiElContenidoEsMasCortoQueElIv() {
        InputStream entradaTruncada = new ByteArrayInputStream(new byte[] {1, 2, 3});

        IOException ex = assertThrows(IOException.class, () -> service.descifrar(entradaTruncada));

        assertEquals(ErrorMessages.ERROR_ARCHIVO_NO_ACCESIBLE, ex.getMessage());
    }
}
