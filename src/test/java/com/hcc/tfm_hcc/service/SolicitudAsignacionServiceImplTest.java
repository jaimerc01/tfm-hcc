package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.SolicitudAsignacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.SolicitudAsignacionServiceImpl;

class SolicitudAsignacionServiceImplTest {

    private SolicitudAsignacionRepository solicitudAsignacionRepository;
    private UsuarioRepository usuarioRepository;
    private NotificacionFacade notificacionFacade;
    private HmacSearchIndexService hmacSearchIndexService;
    private SolicitudAsignacionServiceImpl service;

    @BeforeEach
    void setUp() {
        solicitudAsignacionRepository = mock(SolicitudAsignacionRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        notificacionFacade = mock(NotificacionFacade.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        when(hmacSearchIndexService.indexar(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0, String.class));
        service = new SolicitudAsignacionServiceImpl(solicitudAsignacionRepository, usuarioRepository, notificacionFacade,
                hmacSearchIndexService);
    }

    private Usuario usuarioConNif(String nif) {
        Usuario usuario = new Usuario();
        usuario.setNif(nif);
        usuario.setNifHash("hash-" + nif);
        usuario.setNombre("Nombre" + nif);
        return usuario;
    }

    @Test
    void crearSolicitud_conMedicoYPacienteExistentes_creaYNotifica() {
        Usuario medico = usuarioConNif("11111111A");
        Usuario paciente = usuarioConNif("22222222B");
        when(usuarioRepository.findByNifHash("hash-11111111A")).thenReturn(Optional.of(medico));
        when(usuarioRepository.findByNifHash("hash-22222222B")).thenReturn(Optional.of(paciente));
        when(solicitudAsignacionRepository.existsByMedicoNifHashAndPacienteNifHashAndEstado(
                "hash-11111111A", "hash-22222222B", "PENDIENTE")).thenReturn(false);
        when(solicitudAsignacionRepository.save(any(SolicitudAsignacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitudAsignacion resultado = service.crearSolicitud("11111111A", "22222222B");

        assertEquals("PENDIENTE", resultado.getEstado());
        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(anyString(), anyString());
    }

    @Test
    void crearSolicitud_conMedicoInexistente_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.findByNifHash("hash-11111111A")).thenReturn(Optional.empty());
        when(usuarioRepository.findByNifHash("hash-22222222B")).thenReturn(Optional.of(usuarioConNif("22222222B")));

        assertThrows(UsuarioNoEncontradoException.class, () -> service.crearSolicitud("11111111A", "22222222B"));
    }

    @Test
    void crearSolicitud_conSolicitudPendienteYaExistente_lanzaSolicitudExistenteException() {
        when(usuarioRepository.findByNifHash("hash-11111111A")).thenReturn(Optional.of(usuarioConNif("11111111A")));
        when(usuarioRepository.findByNifHash("hash-22222222B")).thenReturn(Optional.of(usuarioConNif("22222222B")));
        when(solicitudAsignacionRepository.existsByMedicoNifHashAndPacienteNifHashAndEstado(
                "hash-11111111A", "hash-22222222B", "PENDIENTE")).thenReturn(true);

        assertThrows(SolicitudExistenteException.class, () -> service.crearSolicitud("11111111A", "22222222B"));
    }

    @Test
    void crearSolicitud_conFalloAlNotificar_noPropagaLaExcepcionYDevuelveLaSolicitud() {
        when(usuarioRepository.findByNifHash("hash-11111111A")).thenReturn(Optional.of(usuarioConNif("11111111A")));
        when(usuarioRepository.findByNifHash("hash-22222222B")).thenReturn(Optional.of(usuarioConNif("22222222B")));
        when(solicitudAsignacionRepository.existsByMedicoNifHashAndPacienteNifHashAndEstado(
                "hash-11111111A", "hash-22222222B", "PENDIENTE")).thenReturn(false);
        when(solicitudAsignacionRepository.save(any(SolicitudAsignacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionFacade.crearNotificacionParaUsuario(anyString(), anyString()))
                .thenThrow(new RuntimeException("fallo al notificar"));

        SolicitudAsignacion resultado = service.crearSolicitud("11111111A", "22222222B");

        assertEquals("PENDIENTE", resultado.getEstado());
    }

    @Test
    void listarSolicitudesPendientesPorMedico_devuelveLasDelRepositorio() {
        when(solicitudAsignacionRepository.findByMedicoNifHashAndEstado("hash-11111111A", "PENDIENTE"))
                .thenReturn(List.of(new SolicitudAsignacion()));

        List<SolicitudAsignacion> resultado = service.listarSolicitudesPendientesPorMedico("11111111A");

        assertEquals(1, resultado.size());
    }

    @Test
    void listarSolicitudesEnviadasPorMedico_devuelveLasDelRepositorio() {
        when(solicitudAsignacionRepository.findByMedicoNifHashOrderByFechaCreacionDesc("hash-11111111A"))
                .thenReturn(List.of(new SolicitudAsignacion(), new SolicitudAsignacion()));

        List<SolicitudAsignacion> resultado = service.listarSolicitudesEnviadasPorMedico("11111111A");

        assertEquals(2, resultado.size());
    }
}
