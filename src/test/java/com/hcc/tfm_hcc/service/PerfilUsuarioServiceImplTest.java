package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.PerfilUsuarioServiceImpl;

class PerfilUsuarioServiceImplTest {

    private PerfilService perfilService;
    private UsuarioRepository usuarioRepository;
    private PerfilUsuarioRepository perfilUsuarioRepository;
    private PerfilUsuarioServiceImpl service;

    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        perfilService = mock(PerfilService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        perfilUsuarioRepository = mock(PerfilUsuarioRepository.class);
        service = new PerfilUsuarioServiceImpl(perfilService, usuarioRepository, perfilUsuarioRepository);
    }

    @Test
    void asignarPerfil_conUsuarioYPerfilExistentes_guardaLaAsociacion() {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilService.getPerfilByRol("MEDICO")).thenReturn(perfil);
        when(perfilUsuarioRepository.existsByUsuarioIdAndPerfilRol(usuarioId, "MEDICO")).thenReturn(false);

        service.asignarPerfil(usuarioId, "MEDICO");

        verify(perfilUsuarioRepository, times(1)).save(any(PerfilUsuario.class));
    }

    @Test
    void asignarPerfil_conAsociacionYaExistente_noGuardaDeNuevo() {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilService.getPerfilByRol("MEDICO")).thenReturn(perfil);
        when(perfilUsuarioRepository.existsByUsuarioIdAndPerfilRol(usuarioId, "MEDICO")).thenReturn(true);

        service.asignarPerfil(usuarioId, "MEDICO");

        verify(perfilUsuarioRepository, never()).save(any());
    }

    @Test
    void asignarPerfil_conUsuarioInexistente_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.asignarPerfil(usuarioId, "MEDICO"));
    }

    @Test
    void asignarPerfil_conIdUsuarioNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.asignarPerfil(null, "MEDICO"));
    }

    @Test
    void revocarPerfil_conAsociacionExistente_laElimina() {
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");
        PerfilUsuario perfilUsuario = new PerfilUsuario();

        when(perfilService.getPerfilByRol("MEDICO")).thenReturn(perfil);
        when(perfilUsuarioRepository.findByUsuarioIdAndPerfilRol(usuarioId, "MEDICO")).thenReturn(Optional.of(perfilUsuario));

        service.revocarPerfil(usuarioId, "MEDICO");

        verify(perfilUsuarioRepository, times(1)).delete(perfilUsuario);
    }

    @Test
    void revocarPerfil_conAsociacionInexistente_noLanzaExcepcion() {
        when(perfilService.getPerfilByRol("MEDICO")).thenReturn(new Perfil());
        when(perfilUsuarioRepository.findByUsuarioIdAndPerfilRol(usuarioId, "MEDICO")).thenReturn(Optional.empty());

        service.revocarPerfil(usuarioId, "MEDICO");

        verify(perfilUsuarioRepository, never()).delete(any());
    }

    @Test
    void revocarPerfil_conIdUsuarioNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.revocarPerfil(null, "MEDICO"));
    }

    @Test
    void tienePerfil_devuelveElResultadoDelRepositorio() {
        when(perfilService.getPerfilByRol("MEDICO")).thenReturn(new Perfil());
        when(perfilUsuarioRepository.existsByUsuarioIdAndPerfilRol(usuarioId, "MEDICO")).thenReturn(true);

        assertTrue(service.tienePerfil(usuarioId, "MEDICO"));
    }

    @Test
    void tienePerfil_conIdUsuarioNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.tienePerfil(null, "MEDICO"));
    }

    @Test
    void listarUsuariosPorRol_devuelveLosUsuariosDelRepositorio() {
        Usuario usuario = new Usuario();
        when(perfilService.getPerfilByRol("MEDICO")).thenReturn(new Perfil());
        when(perfilUsuarioRepository.findUsuariosByPerfilRol("MEDICO")).thenReturn(List.of(usuario));

        List<Usuario> resultado = service.listarUsuariosPorRol("MEDICO");

        assertEquals(1, resultado.size());
    }
}
