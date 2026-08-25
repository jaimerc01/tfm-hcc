package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.PacienteNoEncontradoException;
import com.hcc.tfm_hcc.exception.SolicitudAsignacionException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.facade.impl.MedicoFacadeImpl;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.service.HistorialClinicoService;
import com.hcc.tfm_hcc.service.MedicoService;

class MedicoFacadeImplTest {

    private MedicoService medicoService;
    private SolicitudAsignacionFacade solicitudAsignacionFacade;
    private HistorialClinicoService historialClinicoService;
    private MedicoFacadeImpl facade;

    @BeforeEach
    void setUp() {
        medicoService = mock(MedicoService.class);
        solicitudAsignacionFacade = mock(SolicitudAsignacionFacade.class);
        historialClinicoService = mock(HistorialClinicoService.class);
        facade = new MedicoFacadeImpl(medicoService, solicitudAsignacionFacade, historialClinicoService);
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conDatosValidos_delegaEnElServicio() {
        PacienteDTO dto = new PacienteDTO();
        when(medicoService.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20")).thenReturn(dto);

        assertEquals(dto, facade.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20"));
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conDniVacio_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class,
                () -> facade.buscarPacientePorDniYFechaNacimiento("  ", "1990-05-20"));
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conFormatoDeFechaInvalido_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class,
                () -> facade.buscarPacientePorDniYFechaNacimiento("12345678A", "20-05-1990"));
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conErrorInesperado_lanzaMedicoOperacionException() {
        when(medicoService.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class,
                () -> facade.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20"));
    }

    @Test
    void obtenerHistorialPaciente_conNifValido_delegaEnElServicioDeHistorial() {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(historialClinicoService.obtenerHistorialPaciente("12345678A")).thenReturn(dto);

        assertEquals(dto, facade.obtenerHistorialPaciente("12345678A"));
    }

    @Test
    void obtenerHistorialPaciente_conNifVacio_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class, () -> facade.obtenerHistorialPaciente("  "));
    }

    @Test
    void obtenerHistorialPaciente_conPacienteNoEncontrado_lanzaPacienteNoEncontradoException() {
        when(historialClinicoService.obtenerHistorialPaciente("12345678A"))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        assertThrows(PacienteNoEncontradoException.class, () -> facade.obtenerHistorialPaciente("12345678A"));
    }

    @Test
    void obtenerHistorialPaciente_sinRelacionActiva_lanzaUsuarioSinPermisoException() {
        when(historialClinicoService.obtenerHistorialPaciente("12345678A"))
                .thenThrow(new IllegalStateException("Acceso denegado"));

        assertThrows(UsuarioSinPermisoException.class, () -> facade.obtenerHistorialPaciente("12345678A"));
    }

    @Test
    void obtenerHistorialPaciente_conErrorInesperado_lanzaMedicoOperacionException() {
        when(historialClinicoService.obtenerHistorialPaciente("12345678A"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.obtenerHistorialPaciente("12345678A"));
    }

    @Test
    void crearSolicitudAsignacion_conNifValido_delegaEnElFacadeDeSolicitudes() {
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setId(UUID.randomUUID());
        when(solicitudAsignacionFacade.crearSolicitudAsignacion("22222222B")).thenReturn(solicitud);

        assertEquals(solicitud, facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void crearSolicitudAsignacion_conNifVacio_lanzaSolicitudAsignacionException() {
        assertThrows(SolicitudAsignacionException.class, () -> facade.crearSolicitudAsignacion("  "));
    }

    @Test
    void crearSolicitudAsignacion_conErrorInesperado_lanzaMedicoOperacionException() {
        when(solicitudAsignacionFacade.crearSolicitudAsignacion("22222222B"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void listarSolicitudesPendientes_delegaEnElFacadeDeSolicitudes() {
        when(solicitudAsignacionFacade.listarSolicitudesPendientes()).thenReturn(List.of(new SolicitudAsignacion()));

        assertEquals(1, facade.listarSolicitudesPendientes().size());
    }

    @Test
    void listarSolicitudesPendientes_conErrorInesperado_lanzaMedicoOperacionException() {
        when(solicitudAsignacionFacade.listarSolicitudesPendientes()).thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.listarSolicitudesPendientes());
    }

    @Test
    void listarSolicitudesEnviadas_delegaEnElFacadeDeSolicitudes() {
        when(solicitudAsignacionFacade.listarSolicitudesEnviadas())
                .thenReturn(List.of(new SolicitudAsignacion(), new SolicitudAsignacion()));

        assertEquals(2, facade.listarSolicitudesEnviadas().size());
    }

    @Test
    void listarSolicitudesEnviadas_conErrorInesperado_lanzaMedicoOperacionException() {
        when(solicitudAsignacionFacade.listarSolicitudesEnviadas()).thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.listarSolicitudesEnviadas());
    }
}
