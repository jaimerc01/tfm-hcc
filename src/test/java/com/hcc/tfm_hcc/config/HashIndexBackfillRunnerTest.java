package com.hcc.tfm_hcc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.DatoClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

/**
 * Pruebas de {@link HashIndexBackfillRunner}: comprueban que al arrancar solo se recalculan
 * los índices HMAC de las filas que aún no los tienen, que es idempotente y que un fallo en
 * el backfill se registra sin abortar el arranque.
 */
class HashIndexBackfillRunnerTest {

    private UsuarioRepository usuarioRepository;
    private DatoClinicoRepository datoClinicoRepository;
    private HmacSearchIndexService hmacSearchIndexService;
    private HashIndexBackfillRunner runner;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        datoClinicoRepository = mock(DatoClinicoRepository.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        when(hmacSearchIndexService.indexar(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0, String.class));
        runner = new HashIndexBackfillRunner(usuarioRepository, datoClinicoRepository, hmacSearchIndexService);
    }

    private Usuario usuario(String nif, String email, String nifHash, String emailHash) {
        Usuario u = new Usuario();
        u.setNif(nif);
        u.setEmail(email);
        u.setNifHash(nifHash);
        u.setEmailHash(emailHash);
        return u;
    }

    @Test
    void run_calculaLosHashesQueFaltanYGuardaSoloEsasFilas() {
        Usuario sinHashes = usuario("12345678A", "a@example.com", null, null);
        Usuario yaMigrado = usuario("87654321B", "b@example.com", "hash-existente", "hash-existente");
        when(usuarioRepository.findAll()).thenReturn(List.of(sinHashes, yaMigrado));

        DatoClinico datoSinHash = new DatoClinico();
        datoSinHash.setTipo("Glucosa");
        DatoClinico datoConHash = new DatoClinico();
        datoConHash.setTipo("Colesterol");
        datoConHash.setTipoHash("hash-existente");
        when(datoClinicoRepository.findAll()).thenReturn(List.of(datoSinHash, datoConHash));

        runner.run();

        assertEquals("hash-12345678A", sinHashes.getNifHash());
        assertEquals("hash-a@example.com", sinHashes.getEmailHash());
        assertEquals("hash-Glucosa", datoSinHash.getTipoHash());
        verify(usuarioRepository, times(1)).save(sinHashes);
        verify(usuarioRepository, never()).save(yaMigrado);
        verify(datoClinicoRepository, times(1)).save(datoSinHash);
        verify(datoClinicoRepository, never()).save(datoConHash);
    }

    @Test
    void run_conTodoMigrado_noGuardaNada() {
        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuario("12345678A", "a@example.com", "h1", "h2")));
        DatoClinico dato = new DatoClinico();
        dato.setTipo("Glucosa");
        dato.setTipoHash("h3");
        when(datoClinicoRepository.findAll()).thenReturn(List.of(dato));

        runner.run();

        verify(usuarioRepository, never()).save(any());
        verify(datoClinicoRepository, never()).save(any());
    }

    @Test
    void run_conUsuarioSinNifNiEmail_noCalculaNada() {
        Usuario vacio = usuario(null, null, null, null);
        when(usuarioRepository.findAll()).thenReturn(List.of(vacio));
        when(datoClinicoRepository.findAll()).thenReturn(List.of());

        runner.run();

        assertNull(vacio.getNifHash());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void run_siFallaElBackfillDeUsuarios_igualIntentaElDeDatosClinicosYNoPropaga() {
        when(usuarioRepository.findAll()).thenThrow(new RuntimeException("falta la columna nif_hash"));
        DatoClinico dato = new DatoClinico();
        dato.setTipo("Glucosa");
        when(datoClinicoRepository.findAll()).thenReturn(List.of(dato));

        runner.run();

        verify(datoClinicoRepository, times(1)).save(dato);
    }

    @Test
    void run_siFallaElBackfillDeDatosClinicos_noPropaga() {
        when(usuarioRepository.findAll()).thenReturn(List.of());
        when(datoClinicoRepository.findAll()).thenThrow(new RuntimeException("falta la columna tipo_hash"));

        runner.run();

        verify(datoClinicoRepository, never()).save(any());
    }
}
