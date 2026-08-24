package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hcc.tfm_hcc.facade.impl.SolicitudAsignacionFacadeImpl;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.SolicitudAsignacionService;

class SolicitudAsignacionFacadeImplTest {

    private SolicitudAsignacionService solicitudAsignacionService;
    private SolicitudAsignacionFacadeImpl facade;

    @BeforeEach
    void setUp() {
        solicitudAsignacionService = mock(SolicitudAsignacionService.class);
        facade = new SolicitudAsignacionFacadeImpl(solicitudAsignacionService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String nif) {
        Usuario usuario = new Usuario();
        usuario.setNif(nif);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null));
    }

    @Test
    void crearSolicitudAsignacion_conMedicoAutenticado_delegaEnElServicio() {
        autenticarComo("11111111A");
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        when(solicitudAsignacionService.crearSolicitud("11111111A", "22222222B")).thenReturn(solicitud);

        assertEquals(solicitud, facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void crearSolicitudAsignacion_sinMedicoAutenticado_devuelveNull() {
        assertNull(facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void crearSolicitudAsignacion_conErrorDelServicio_propagaLaExcepcion() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.crearSolicitud("11111111A", "22222222B"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(RuntimeException.class, () -> facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void listarSolicitudesPendientes_conMedicoAutenticado_delegaEnElServicio() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.listarSolicitudesPendientesPorMedico("11111111A"))
                .thenReturn(List.of(new SolicitudAsignacion()));

        assertEquals(1, facade.listarSolicitudesPendientes().size());
    }

    @Test
    void listarSolicitudesPendientes_sinAutenticar_devuelveListaVacia() {
        assertTrue(facade.listarSolicitudesPendientes().isEmpty());
    }

    @Test
    void listarSolicitudesEnviadas_conMedicoAutenticado_delegaEnElServicio() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.listarSolicitudesEnviadasPorMedico("11111111A"))
                .thenReturn(List.of(new SolicitudAsignacion(), new SolicitudAsignacion()));

        assertEquals(2, facade.listarSolicitudesEnviadas().size());
    }

    @Test
    void listarSolicitudesEnviadas_sinAutenticar_devuelveListaVacia() {
        assertTrue(facade.listarSolicitudesEnviadas().isEmpty());
    }
}
