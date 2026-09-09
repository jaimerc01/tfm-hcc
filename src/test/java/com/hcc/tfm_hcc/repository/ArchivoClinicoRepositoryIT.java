package com.hcc.tfm_hcc.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.hcc.tfm_hcc.model.ArchivoClinico;

/**
 * Verifica, contra un MongoDB embebido real (no mocks), que
 * {@link ArchivoClinico} se guarda y recupera correctamente con un
 * {@code UUID} como {@code @Id} y con contenido binario en un campo
 * {@code byte[]}. El resto de documentos Mongo del proyecto usan
 * {@code String} como tipo de {@code @Id}; este es el primero con
 * {@code UUID}, de ahí la necesidad de una prueba de integración además
 * de las pruebas unitarias con repositorio mockeado de
 * {@code ArchivoClinicoServiceImplTest}.
 */
@DataMongoTest
class ArchivoClinicoRepositoryIT {

    @Autowired
    private ArchivoClinicoRepository archivoClinicoRepository;

    @Test
    void save_yFindById_conservaElUuidYElContenidoBinario() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        byte[] contenido = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(id);
        archivo.setUsuarioId(usuarioId);
        archivo.setNombreOriginal("cifrado-de-prueba");
        archivo.setContentType("application/pdf");
        archivo.setSizeBytes((long) contenido.length);
        archivo.setContenido(contenido);
        archivo.setFechaCreacion(LocalDateTime.now());

        archivoClinicoRepository.save(archivo);

        Optional<ArchivoClinico> recuperado = archivoClinicoRepository.findById(id);

        assertTrue(recuperado.isPresent());
        assertEquals(id, recuperado.get().getId());
        assertEquals(usuarioId, recuperado.get().getUsuarioId());
        assertArrayEquals(contenido, recuperado.get().getContenido());
    }

    @Test
    void findByUsuarioIdOrderByFechaCreacionDesc_devuelveSoloLosDelUsuarioOrdenados() {
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime ahora = LocalDateTime.now();

        ArchivoClinico masAntiguo = nuevoArchivo(usuarioId, ahora.minusDays(1));
        ArchivoClinico masReciente = nuevoArchivo(usuarioId, ahora);
        ArchivoClinico deOtroUsuario = nuevoArchivo(UUID.randomUUID(), ahora);

        archivoClinicoRepository.saveAll(List.of(masAntiguo, masReciente, deOtroUsuario));

        List<ArchivoClinico> resultado = archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);

        assertEquals(2, resultado.size());
        assertEquals(masReciente.getId(), resultado.get(0).getId());
        assertEquals(masAntiguo.getId(), resultado.get(1).getId());
    }

    @Test
    void findByIdAndUsuarioId_vacioSiElArchivoPerteneceAOtroUsuario() {
        ArchivoClinico archivo = nuevoArchivo(UUID.randomUUID(), LocalDateTime.now());
        archivoClinicoRepository.save(archivo);

        Optional<ArchivoClinico> resultado =
                archivoClinicoRepository.findByIdAndUsuarioId(archivo.getId(), UUID.randomUUID());

        assertTrue(resultado.isEmpty());
    }

    private ArchivoClinico nuevoArchivo(UUID usuarioId, LocalDateTime fechaCreacion) {
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());
        archivo.setUsuarioId(usuarioId);
        archivo.setNombreOriginal("archivo");
        archivo.setContentType("application/pdf");
        archivo.setSizeBytes(1L);
        archivo.setContenido(new byte[] {9});
        archivo.setFechaCreacion(fechaCreacion);
        return archivo;
    }
}
