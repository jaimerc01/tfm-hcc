package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.AdminOperacionException;
import com.hcc.tfm_hcc.exception.AdminValidationException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.facade.impl.AdminFacadeImpl;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.MedicoService;

class AdminFacadeImplTest {

    private MedicoService medicoService;
    private UsuarioRepository usuarioRepository;
    private UsuarioMapper usuarioMapper;
    private HmacSearchIndexService hmacSearchIndexService;
    private AdminFacadeImpl facade;

    @BeforeEach
    void setUp() {
        medicoService = mock(MedicoService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioMapper = mock(UsuarioMapper.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        facade = new AdminFacadeImpl(medicoService, usuarioRepository, usuarioMapper, hmacSearchIndexService);
    }

    private UsuarioDTO medicoValido() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif("12345678A");
        dto.setNombre("Ana");
        dto.setApellido1("García");
        dto.setEmail("ana@example.com");
        return dto;
    }

    @Test
    void listarMedicos_devuelveLaListaDelServicio() {
        when(medicoService.listarMedicos()).thenReturn(List.of(new UsuarioDTO()));

        assertEquals(1, facade.listarMedicos().size());
    }

    @Test
    void listarMedicos_conErrorDelServicio_lanzaAdminOperacionException() {
        when(medicoService.listarMedicos()).thenThrow(new RuntimeException("fallo"));

        assertThrows(AdminOperacionException.class, () -> facade.listarMedicos());
    }

    @Test
    void crearMedico_conDatosValidos_devuelveElMedicoCreado() {
        UsuarioDTO entrada = medicoValido();
        UsuarioDTO creado = new UsuarioDTO();
        when(medicoService.crearMedico(entrada)).thenReturn(creado);

        assertEquals(creado, facade.crearMedico(entrada));
    }

    @Test
    void crearMedico_conDtoNulo_lanzaAdminValidationException() {
        assertThrows(AdminValidationException.class, () -> facade.crearMedico(null));
    }

    @Test
    void crearMedico_conNombreVacio_lanzaAdminValidationException() {
        UsuarioDTO dto = medicoValido();
        dto.setNombre("  ");

        assertThrows(AdminValidationException.class, () -> facade.crearMedico(dto));
    }

    @Test
    void crearMedico_conErrorInesperado_lanzaAdminOperacionException() {
        UsuarioDTO dto = medicoValido();
        when(medicoService.crearMedico(dto)).thenThrow(new RuntimeException("fallo"));

        assertThrows(AdminOperacionException.class, () -> facade.crearMedico(dto));
    }

    @Test
    void actualizarMedico_conDatosValidos_devuelveElMedicoActualizado() {
        UUID id = UUID.randomUUID();
        UsuarioDTO entrada = medicoValido();
        UsuarioDTO actualizado = new UsuarioDTO();
        when(medicoService.actualizarMedico(id, entrada)).thenReturn(actualizado);

        assertEquals(actualizado, facade.actualizarMedico(id, entrada));
    }

    @Test
    void actualizarMedico_conIdNulo_lanzaAdminValidationException() {
        assertThrows(AdminValidationException.class, () -> facade.actualizarMedico(null, medicoValido()));
    }

    @Test
    void eliminarMedico_conIdValido_devuelveElIdEliminado() {
        UUID id = UUID.randomUUID();
        when(medicoService.eliminarMedico(id)).thenReturn(id);

        assertEquals(id, facade.eliminarMedico(id));
    }

    @Test
    void eliminarMedico_conIdNulo_lanzaAdminValidationException() {
        assertThrows(AdminValidationException.class, () -> facade.eliminarMedico(null));
    }

    @Test
    void eliminarMedico_conErrorInesperado_lanzaAdminOperacionException() {
        UUID id = UUID.randomUUID();
        when(medicoService.eliminarMedico(id)).thenThrow(new RuntimeException("fallo"));

        assertThrows(AdminOperacionException.class, () -> facade.eliminarMedico(id));
    }

    @Test
    void setPerfilMedico_conIdValido_devuelveElIdAfectado() {
        UUID id = UUID.randomUUID();

        assertEquals(id, facade.setPerfilMedico(id, true));
    }

    @Test
    void setPerfilMedico_conIdNulo_lanzaAdminValidationException() {
        assertThrows(AdminValidationException.class, () -> facade.setPerfilMedico(null, true));
    }

    @Test
    void buscarUsuarioPorNif_conUsuarioExistente_devuelveElDto() {
        Usuario usuario = new Usuario();
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(usuario)).thenReturn(dto);

        assertEquals(dto, facade.buscarUsuarioPorNif("12345678A"));
    }

    @Test
    void buscarUsuarioPorNif_conUsuarioInexistente_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> facade.buscarUsuarioPorNif("12345678A"));
    }

    /**
     * Regresión: el mensaje de la excepción se vuelve a registrar en el log justo
     * después de lanzarla, así que debe llevar el NIF enmascarado, no en claro.
     */
    @Test
    void buscarUsuarioPorNif_conUsuarioInexistente_elMensajeEnmascaraElNif() {
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.empty());

        UsuarioNoEncontradoException ex = assertThrows(UsuarioNoEncontradoException.class,
                () -> facade.buscarUsuarioPorNif("12345678A"));

        org.junit.jupiter.api.Assertions.assertFalse(ex.getMessage().contains("12345678A"));
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("***78A"));
    }

    @Test
    void buscarUsuarioPorNif_conNifVacio_lanzaAdminValidationException() {
        assertThrows(AdminValidationException.class, () -> facade.buscarUsuarioPorNif("  "));
    }

    @Test
    void buscarUsuarioPorNif_conNifDemasiadoCorto_lanzaAdminValidationException() {
        assertThrows(AdminValidationException.class, () -> facade.buscarUsuarioPorNif("123"));
    }
}
