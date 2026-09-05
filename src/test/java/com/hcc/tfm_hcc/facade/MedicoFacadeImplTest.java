package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.PacienteNoEncontradoException;
import com.hcc.tfm_hcc.exception.SolicitudAsignacionException;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.converter.AnotacionMedicaConverter;
import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.facade.impl.MedicoFacadeImpl;
import com.hcc.tfm_hcc.mapper.ArchivoClinicoMapper;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.AnotacionMedicaService;
import com.hcc.tfm_hcc.service.ArchivoClinicoService;
import com.hcc.tfm_hcc.service.HistorialClinicoService;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.service.SolicitudAsignacionService;

class MedicoFacadeImplTest {

    private MedicoService medicoService;
    private SolicitudAsignacionService solicitudAsignacionService;
    private HistorialClinicoService historialClinicoService;
    private AnotacionMedicaService anotacionMedicaService;
    private AnotacionMedicaConverter anotacionMedicaConverter;
    private ArchivoClinicoService archivoClinicoService;
    private ArchivoClinicoMapper archivoClinicoMapper;
    private NotificacionFacade notificacionFacade;
    private MedicoFacadeImpl facade;

    @BeforeEach
    void setUp() {
        medicoService = mock(MedicoService.class);
        solicitudAsignacionService = mock(SolicitudAsignacionService.class);
        historialClinicoService = mock(HistorialClinicoService.class);
        anotacionMedicaService = mock(AnotacionMedicaService.class);
        anotacionMedicaConverter = mock(AnotacionMedicaConverter.class);
        archivoClinicoService = mock(ArchivoClinicoService.class);
        archivoClinicoMapper = mock(ArchivoClinicoMapper.class);
        notificacionFacade = mock(NotificacionFacade.class);
        facade = new MedicoFacadeImpl(medicoService, solicitudAsignacionService, historialClinicoService,
                anotacionMedicaService, anotacionMedicaConverter, archivoClinicoService, archivoClinicoMapper, notificacionFacade);
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

    // ---- búsqueda de paciente ----

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

    // ---- historial de paciente ----

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

    // ---- solicitudes de asignación (orquesta SolicitudAsignacionService directamente) ----

    @Test
    void crearSolicitudAsignacion_conMedicoAutenticado_delegaEnElServicio() {
        autenticarComo("11111111A");
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setId(UUID.randomUUID());
        when(solicitudAsignacionService.crearSolicitud("11111111A", "22222222B")).thenReturn(solicitud);

        assertEquals(solicitud, facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void crearSolicitudAsignacion_sinMedicoAutenticado_lanzaUsuarioNoAutenticadoException() {
        assertThrows(UsuarioNoAutenticadoException.class, () -> facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void crearSolicitudAsignacion_conNifVacio_lanzaSolicitudAsignacionException() {
        autenticarComo("11111111A");
        assertThrows(SolicitudAsignacionException.class, () -> facade.crearSolicitudAsignacion("  "));
    }

    @Test
    void crearSolicitudAsignacion_conErrorInesperado_lanzaMedicoOperacionException() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.crearSolicitud("11111111A", "22222222B"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void crearSolicitudAsignacion_conSolicitudYaExistente_propagaSolicitudExistenteException() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.crearSolicitud("11111111A", "22222222B"))
                .thenThrow(new SolicitudExistenteException("ya existe una pendiente"));

        // No debe quedar sepultada en un MedicoOperacionException (que daría un 500 en vez de un 409).
        assertThrows(SolicitudExistenteException.class, () -> facade.crearSolicitudAsignacion("22222222B"));
    }

    @Test
    void listarSolicitudesPendientes_conMedicoAutenticado_delegaEnElServicio() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.listarSolicitudesPendientesPorMedico("11111111A"))
                .thenReturn(List.of(new SolicitudAsignacion()));

        assertEquals(1, facade.listarSolicitudesPendientes().size());
    }

    @Test
    void listarSolicitudesPendientes_sinMedicoAutenticado_devuelveListaVacia() {
        assertTrue(facade.listarSolicitudesPendientes().isEmpty());
    }

    @Test
    void listarSolicitudesPendientes_conErrorInesperado_lanzaMedicoOperacionException() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.listarSolicitudesPendientesPorMedico("11111111A"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.listarSolicitudesPendientes());
    }

    @Test
    void listarSolicitudesEnviadas_conMedicoAutenticado_delegaEnElServicio() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.listarSolicitudesEnviadasPorMedico("11111111A"))
                .thenReturn(List.of(new SolicitudAsignacion(), new SolicitudAsignacion()));

        assertEquals(2, facade.listarSolicitudesEnviadas().size());
    }

    @Test
    void listarSolicitudesEnviadas_sinMedicoAutenticado_devuelveListaVacia() {
        assertTrue(facade.listarSolicitudesEnviadas().isEmpty());
    }

    @Test
    void listarSolicitudesEnviadas_conErrorInesperado_lanzaMedicoOperacionException() {
        autenticarComo("11111111A");
        when(solicitudAsignacionService.listarSolicitudesEnviadasPorMedico("11111111A"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.listarSolicitudesEnviadas());
    }

    // ---- anotaciones médicas ----

    @Test
    void crearAnotacion_conMedicoAutenticado_delegaEnElServicioYNotificaAlPaciente() {
        autenticarComo("11111111A");
        Usuario medico = new Usuario();
        medico.setNif("11111111A");
        medico.setNombre("Ana");
        Usuario paciente = new Usuario();
        paciente.setNif("22222222B");
        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setId(UUID.randomUUID());
        anotacion.setMedico(medico);
        anotacion.setPaciente(paciente);
        when(anotacionMedicaService.crearAnotacion("11111111A", "22222222B", "Revisar tensión"))
                .thenReturn(anotacion);

        assertEquals(anotacion, facade.crearAnotacion("22222222B", "Revisar tensión"));
        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq("22222222B"), anyString());
    }

    @Test
    void crearAnotacion_conFalloAlNotificar_noPropagaLaExcepcionYDevuelveLaAnotacion() {
        autenticarComo("11111111A");
        Usuario medico = new Usuario();
        medico.setNif("11111111A");
        Usuario paciente = new Usuario();
        paciente.setNif("22222222B");
        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setMedico(medico);
        anotacion.setPaciente(paciente);
        when(anotacionMedicaService.crearAnotacion("11111111A", "22222222B", "texto")).thenReturn(anotacion);
        when(notificacionFacade.crearNotificacionParaUsuario(anyString(), anyString()))
                .thenThrow(new RuntimeException("fallo al notificar"));

        assertEquals(anotacion, facade.crearAnotacion("22222222B", "texto"));
    }

    @Test
    void crearAnotacion_sinMedicoAutenticado_lanzaUsuarioNoAutenticadoException() {
        assertThrows(UsuarioNoAutenticadoException.class, () -> facade.crearAnotacion("22222222B", "texto"));
    }

    @Test
    void crearAnotacion_conNifPacienteVacio_lanzaMedicoValidationException() {
        autenticarComo("11111111A");
        assertThrows(MedicoValidationException.class, () -> facade.crearAnotacion("  ", "texto"));
    }

    @Test
    void crearAnotacion_conMensajeVacio_propagaMedicoValidationException() {
        autenticarComo("11111111A");
        when(anotacionMedicaService.crearAnotacion("11111111A", "22222222B", "  "))
                .thenThrow(new MedicoValidationException("mensaje requerido"));

        assertThrows(MedicoValidationException.class, () -> facade.crearAnotacion("22222222B", "  "));
    }

    @Test
    void crearAnotacion_sinRelacionActiva_lanzaUsuarioSinPermisoException() {
        autenticarComo("11111111A");
        when(anotacionMedicaService.crearAnotacion("11111111A", "22222222B", "texto"))
                .thenThrow(new IllegalStateException("Acceso denegado"));

        assertThrows(UsuarioSinPermisoException.class, () -> facade.crearAnotacion("22222222B", "texto"));
    }

    @Test
    void crearAnotacion_conPacienteNoEncontrado_lanzaPacienteNoEncontradoException() {
        autenticarComo("11111111A");
        when(anotacionMedicaService.crearAnotacion("11111111A", "22222222B", "texto"))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        assertThrows(PacienteNoEncontradoException.class, () -> facade.crearAnotacion("22222222B", "texto"));
    }

    @Test
    void crearAnotacion_conErrorInesperado_lanzaMedicoOperacionException() {
        autenticarComo("11111111A");
        when(anotacionMedicaService.crearAnotacion("11111111A", "22222222B", "texto"))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.crearAnotacion("22222222B", "texto"));
    }

    // ---- listar anotaciones del médico sobre un paciente ----

    @Test
    void listarAnotacionesPaciente_delegaEnElServicioYConvierteADto() {
        autenticarComo("11111111A");
        AnotacionMedica anotacion = new AnotacionMedica();
        when(anotacionMedicaService.listarAnotacionesEscritasPorMedico("11111111A", "22222222B"))
                .thenReturn(java.util.List.of(anotacion));
        when(anotacionMedicaConverter.toDtoList(java.util.List.of(anotacion)))
                .thenReturn(java.util.List.of(new AnotacionMedicaDTO()));

        assertEquals(1, facade.listarAnotacionesPaciente("22222222B").size());
    }

    @Test
    void listarAnotacionesPaciente_sinAccesoActivo_lanzaUsuarioSinPermisoException() {
        autenticarComo("11111111A");
        when(anotacionMedicaService.listarAnotacionesEscritasPorMedico("11111111A", "22222222B"))
                .thenThrow(new IllegalStateException("acceso denegado"));

        assertThrows(UsuarioSinPermisoException.class, () -> facade.listarAnotacionesPaciente("22222222B"));
    }

    @Test
    void listarAnotacionesPaciente_conPacienteInexistente_lanzaPacienteNoEncontradoException() {
        autenticarComo("11111111A");
        when(anotacionMedicaService.listarAnotacionesEscritasPorMedico("11111111A", "22222222B"))
                .thenThrow(new IllegalArgumentException("paciente no encontrado"));

        assertThrows(PacienteNoEncontradoException.class, () -> facade.listarAnotacionesPaciente("22222222B"));
    }

    @Test
    void listarAnotacionesPaciente_sinUsuarioAutenticado_lanzaUsuarioNoAutenticadoException() {
        assertThrows(UsuarioNoAutenticadoException.class, () -> facade.listarAnotacionesPaciente("22222222B"));
    }

    // ---- documentos del paciente ----

    @Test
    void listarArchivosPaciente_delegaEnElServicioYConvierteADto() {
        com.hcc.tfm_hcc.model.ArchivoClinico archivo = new com.hcc.tfm_hcc.model.ArchivoClinico();
        when(archivoClinicoService.listForPaciente("22222222B")).thenReturn(java.util.List.of(archivo));
        when(archivoClinicoMapper.toDto(archivo)).thenReturn(new com.hcc.tfm_hcc.dto.ArchivoClinicoDTO());

        assertEquals(1, facade.listarArchivosPaciente("22222222B").size());
    }

    @Test
    void listarArchivosPaciente_sinAccesoActivo_propagaUsuarioSinPermisoException() {
        when(archivoClinicoService.listForPaciente("22222222B"))
                .thenThrow(new UsuarioSinPermisoException("acceso denegado"));

        assertThrows(UsuarioSinPermisoException.class, () -> facade.listarArchivosPaciente("22222222B"));
    }

    @Test
    void subirArchivoPaciente_delegaEnElServicioYNotificaAlPaciente() throws Exception {
        autenticarComo("11111111A");
        com.hcc.tfm_hcc.model.ArchivoClinico guardado = new com.hcc.tfm_hcc.model.ArchivoClinico();
        guardado.setId(UUID.randomUUID());
        org.springframework.web.multipart.MultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes());
        when(archivoClinicoService.uploadForPaciente("22222222B", file)).thenReturn(guardado);
        when(archivoClinicoMapper.toDto(guardado)).thenReturn(new com.hcc.tfm_hcc.dto.ArchivoClinicoDTO());

        facade.subirArchivoPaciente("22222222B", file);

        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq("22222222B"), anyString());
    }

    @Test
    void obtenerArchivoPaciente_conIdInexistente_lanzaIllegalArgumentException() {
        when(archivoClinicoService.listForPaciente("22222222B")).thenReturn(java.util.List.of());

        assertThrows(IllegalArgumentException.class,
                () -> facade.obtenerArchivoPaciente("22222222B", UUID.randomUUID()));
    }

    @Test
    void descargarArchivoPaciente_delegaEnElServicio() {
        org.springframework.core.io.Resource recurso =
                new org.springframework.core.io.ByteArrayResource("x".getBytes());
        UUID id = UUID.randomUUID();
        when(archivoClinicoService.getPacienteResource("22222222B", id)).thenReturn(recurso);

        assertEquals(recurso, facade.descargarArchivoPaciente("22222222B", id));
    }

    @Test
    void subirArchivoPaciente_conArchivoInvalido_propagaIllegalArgumentException() throws Exception {
        autenticarComo("11111111A");
        org.springframework.web.multipart.MultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", new byte[0]);
        when(archivoClinicoService.uploadForPaciente("22222222B", file))
                .thenThrow(new IllegalArgumentException("archivo vacío"));

        assertThrows(IllegalArgumentException.class, () -> facade.subirArchivoPaciente("22222222B", file));
    }

    @Test
    void subirArchivoPaciente_conFalloDeCifrado_lanzaArchivoClinicoException() throws Exception {
        autenticarComo("11111111A");
        org.springframework.web.multipart.MultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes());
        when(archivoClinicoService.uploadForPaciente("22222222B", file))
                .thenThrow(new java.io.IOException("fallo de cifrado"));

        assertThrows(com.hcc.tfm_hcc.exception.ArchivoClinicoException.class,
                () -> facade.subirArchivoPaciente("22222222B", file));
    }

    @Test
    void listarArchivosPaciente_conErrorInesperado_lanzaMedicoOperacionException() {
        when(archivoClinicoService.listForPaciente("22222222B")).thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> facade.listarArchivosPaciente("22222222B"));
    }
}
