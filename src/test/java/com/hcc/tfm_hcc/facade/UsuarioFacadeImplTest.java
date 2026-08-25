package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.ReautenticacionRequeridaException;
import com.hcc.tfm_hcc.facade.impl.UsuarioFacadeImpl;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.UsuarioService;

class UsuarioFacadeImplTest {

    private UsuarioService usuarioService;
    private UsuarioMapper usuarioMapper;
    private UsuarioFacadeImpl facade;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        usuarioMapper = mock(UsuarioMapper.class);
        facade = new UsuarioFacadeImpl(usuarioService, usuarioMapper);
    }

    private UsuarioDTO usuarioDtoConNif(String nif) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif(nif);
        return dto;
    }

    @Test
    void altaUsuario_conDatosValidos_devuelveElDtoConvertido() {
        UsuarioDTO entrada = usuarioDtoConNif("12345678A");
        Usuario creado = new Usuario();
        UsuarioDTO resultado = new UsuarioDTO();
        when(usuarioService.altaUsuario(entrada)).thenReturn(creado);
        when(usuarioMapper.toDto(creado)).thenReturn(resultado);

        assertEquals(resultado, facade.altaUsuario(entrada));
    }

    @Test
    void altaUsuario_conDtoNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> facade.altaUsuario(null));
    }

    @Test
    void altaUsuario_conNifVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> facade.altaUsuario(usuarioDtoConNif("  ")));
    }

    @Test
    void altaUsuario_conErrorInesperado_lanzaRuntimeException() {
        UsuarioDTO entrada = usuarioDtoConNif("12345678A");
        when(usuarioService.altaUsuario(entrada)).thenThrow(new IllegalStateException("fallo"));

        assertThrows(RuntimeException.class, () -> facade.altaUsuario(entrada));
    }

    @Test
    void getNombreUsuario_delegaEnElServicio() {
        when(usuarioService.getNombreUsuario()).thenReturn("Ana García");

        assertEquals("Ana García", facade.getNombreUsuario());
    }

    @Test
    void getUsuarioActual_delegaEnElServicio() {
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioService.getUsuarioActual()).thenReturn(dto);

        assertEquals(dto, facade.getUsuarioActual());
    }

    @Test
    void changePassword_conDatosValidos_delegaEnElServicio() {
        facade.changePassword("actual123", "nuevaContrasena1");

        verify(usuarioService, times(1)).changePassword("actual123", "nuevaContrasena1");
    }

    @Test
    void changePassword_conPasswordActualVacia_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> facade.changePassword("  ", "nuevaContrasena1"));
    }

    @Test
    void changePassword_conNuevaPasswordDemasiadoCorta_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> facade.changePassword("actual123", "corta"));
    }

    @Test
    void updateUsuarioActual_conDatosValidos_devuelveElResultado() {
        UsuarioDTO parcial = new UsuarioDTO();
        UsuarioDTO resultado = new UsuarioDTO();
        when(usuarioService.updateUsuarioActual(parcial)).thenReturn(resultado);

        assertEquals(resultado, facade.updateUsuarioActual(parcial));
    }

    @Test
    void updateUsuarioActual_conParcialNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> facade.updateUsuarioActual(null));
    }

    @Test
    void deleteCuentaActual_delegaEnElServicio() {
        facade.deleteCuentaActual("miContraseñaActual");

        verify(usuarioService, times(1)).deleteCuentaActual("miContraseñaActual");
    }

    @Test
    void deleteCuentaActual_conReautenticacionRequerida_propagaLaExcepcion() {
        org.mockito.Mockito.doThrow(new ReautenticacionRequeridaException("reautenticación requerida"))
                .when(usuarioService).deleteCuentaActual(any());

        assertThrows(ReautenticacionRequeridaException.class, () -> facade.deleteCuentaActual("incorrecta"));
    }

    @Test
    void getMisLogs_conRangoValido_delegaEnElServicio() {
        LocalDateTime desde = LocalDateTime.now().minusDays(1);
        LocalDateTime hasta = LocalDateTime.now();
        when(usuarioService.getMisLogs(desde, hasta)).thenReturn(List.of());

        assertEquals(0, facade.getMisLogs(desde, hasta).size());
    }

    @Test
    void getMisLogs_conFechaInicioPosteriorAFechaFin_lanzaIllegalArgumentException() {
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = LocalDateTime.now().minusDays(1);

        assertThrows(IllegalArgumentException.class, () -> facade.getMisLogs(desde, hasta));
    }

    @Test
    void exportUsuario_delegaEnElServicio() {
        UserExportDTO export = UserExportDTO.builder().build();
        when(usuarioService.exportUsuario("miContraseñaActual")).thenReturn(export);

        assertEquals(export, facade.exportUsuario("miContraseñaActual"));
    }

    @Test
    void listarMisSolicitudes_delegaEnElServicio() {
        when(usuarioService.listarMisSolicitudes()).thenReturn(List.of(new SolicitudAsignacion()));

        assertEquals(1, facade.listarMisSolicitudes().size());
    }

    @Test
    void actualizarEstadoSolicitud_conDatosValidos_delegaEnElServicio() {
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        when(usuarioService.actualizarEstadoSolicitud("id-1", "ACEPTADA")).thenReturn(solicitud);

        assertEquals(solicitud, facade.actualizarEstadoSolicitud("id-1", "ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_conIdVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> facade.actualizarEstadoSolicitud("  ", "ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_conEstadoVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> facade.actualizarEstadoSolicitud("id-1", "  "));
    }
}
