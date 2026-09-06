package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoRequestDTO;
import com.hcc.tfm_hcc.exception.PropuestaCambioClinicoException;
import com.hcc.tfm_hcc.exception.PropuestaCambioNoEncontradaException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico.Dominio;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico.Operacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PropuestaCambioClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.PropuestaCambioClinicoServiceImpl;

class PropuestaCambioClinicoServiceImplTest {

    private static final String NIF_MEDICO = "11111111A";
    private static final String NIF_PACIENTE = "22222222B";
    private static final String NIF_OTRO_PACIENTE = "33333333C";

    private PropuestaCambioClinicoRepository propuestaRepository;
    private UsuarioRepository usuarioRepository;
    private HmacSearchIndexService hmacSearchIndexService;
    private RelacionMedicoPacienteService relacionMedicoPacienteService;
    private HistorialClinicoService historialClinicoService;
    private AuditoriaCambioService auditoriaCambioService;
    private NotificacionFacade notificacionFacade;
    private PropuestaCambioClinicoServiceImpl service;

    private Usuario medico;
    private Usuario paciente;
    private HistorialClinico historial;

    @BeforeEach
    void setUp() {
        propuestaRepository = mock(PropuestaCambioClinicoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        relacionMedicoPacienteService = mock(RelacionMedicoPacienteService.class);
        historialClinicoService = mock(HistorialClinicoService.class);
        auditoriaCambioService = mock(AuditoriaCambioService.class);
        notificacionFacade = mock(NotificacionFacade.class);

        when(hmacSearchIndexService.indexar(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0, String.class));

        service = new PropuestaCambioClinicoServiceImpl(propuestaRepository, usuarioRepository, hmacSearchIndexService,
                relacionMedicoPacienteService, historialClinicoService, auditoriaCambioService, notificacionFacade,
                new ObjectMapper());

        medico = usuarioConNif(NIF_MEDICO);
        medico.setNombre("Ana");
        paciente = usuarioConNif(NIF_PACIENTE);
        historial = new HistorialClinico();
        historial.setId(UUID.randomUUID());
        historial.setUsuario(paciente);

        when(usuarioRepository.findByNifHash("hash-" + NIF_MEDICO)).thenReturn(Optional.of(medico));
        when(usuarioRepository.findByNifHash("hash-" + NIF_PACIENTE)).thenReturn(Optional.of(paciente));
        when(relacionMedicoPacienteService.verificarAccesoMedicoActivo(NIF_MEDICO, NIF_PACIENTE)).thenReturn(paciente);
        when(historialClinicoService.asegurarHistorial(paciente.getId())).thenReturn(historial);
        when(propuestaRepository.save(any(PropuestaCambioClinico.class))).thenAnswer(inv -> {
            PropuestaCambioClinico p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(UUID.randomUUID());
            }
            return p;
        });
    }

    private Usuario usuarioConNif(String nif) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif(nif);
        usuario.setNifHash("hash-" + nif);
        return usuario;
    }

    private PropuestaCambioClinicoRequestDTO peticionAltaMedicion() {
        PropuestaCambioClinicoRequestDTO request = new PropuestaCambioClinicoRequestDTO();
        request.setDominio("ANALISIS_SANGRE");
        request.setOperacion("CREATE");
        request.setMotivo("Corrección tras revisión en consulta");
        DatoClinicoEntradaDTO medicion = new DatoClinicoEntradaDTO();
        medicion.setLabel("Glucosa");
        medicion.setValue("95");
        medicion.setUnit("mg/dL");
        request.setMedicion(medicion);
        return request;
    }

    // ---- crearPropuesta ----

    @Test
    void crearPropuesta_altaMedicion_persistePendienteNotificaAlPacienteYAudita() {
        PropuestaCambioClinico creada = service.crearPropuesta(NIF_MEDICO, NIF_PACIENTE, peticionAltaMedicion());

        assertEquals(PropuestaCambioClinico.ESTADO_PENDIENTE, creada.getEstado());
        assertEquals(Dominio.ANALISIS_SANGRE, creada.getDominio());
        assertEquals(Operacion.CREATE, creada.getOperacion());
        assertEquals(medico, creada.getMedico());
        assertEquals(paciente, creada.getPaciente());
        assertEquals(historial, creada.getHistorialClinico());
        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq(NIF_PACIENTE), anyString());
        verify(auditoriaCambioService, times(1)).registrarCambio(
                eq(medico.getId().toString()), eq(paciente.getId().toString()), eq(medico.getId().toString()),
                eq("PROPUESTA_CAMBIO_CLINICO"), eq("propuesta_cambio_clinico"), anyString(),
                anyString(), anyString(), eq(AuditoriaCambio.TipoOperacion.CREATE), anyString());
        verify(historialClinicoService, never()).aplicarCambioMedicion(any(), any(), any(), any(), any(), anyString());
    }

    @Test
    void crearPropuesta_sinMotivo_lanzaExcepcionYNoPersiste() {
        PropuestaCambioClinicoRequestDTO request = peticionAltaMedicion();
        request.setMotivo("   ");

        assertThrows(PropuestaCambioClinicoException.class,
                () -> service.crearPropuesta(NIF_MEDICO, NIF_PACIENTE, request));
        verify(propuestaRepository, never()).save(any());
    }

    @Test
    void crearPropuesta_sinRelacionActiva_propagaUsuarioSinPermiso() {
        when(relacionMedicoPacienteService.verificarAccesoMedicoActivo(NIF_MEDICO, NIF_PACIENTE))
                .thenThrow(new UsuarioSinPermisoException("Acceso denegado"));

        assertThrows(UsuarioSinPermisoException.class,
                () -> service.crearPropuesta(NIF_MEDICO, NIF_PACIENTE, peticionAltaMedicion()));
        verify(propuestaRepository, never()).save(any());
    }

    @Test
    void crearPropuesta_dominioInvalido_lanzaExcepcion() {
        PropuestaCambioClinicoRequestDTO request = peticionAltaMedicion();
        request.setDominio("NO_EXISTE");

        assertThrows(PropuestaCambioClinicoException.class,
                () -> service.crearPropuesta(NIF_MEDICO, NIF_PACIENTE, request));
    }

    @Test
    void crearPropuesta_edicionSinRecursoObjetivo_lanzaExcepcion() {
        PropuestaCambioClinicoRequestDTO request = peticionAltaMedicion();
        request.setOperacion("UPDATE");
        request.setIdRecursoObjetivo(null);

        assertThrows(PropuestaCambioClinicoException.class,
                () -> service.crearPropuesta(NIF_MEDICO, NIF_PACIENTE, request));
    }

    @Test
    void crearPropuesta_edicionDeAlergia_lanzaExcepcion() {
        PropuestaCambioClinicoRequestDTO request = new PropuestaCambioClinicoRequestDTO();
        request.setDominio("ALERGIA");
        request.setOperacion("UPDATE");
        request.setMotivo("motivo");
        request.setIdRecursoObjetivo(UUID.randomUUID().toString());
        AlergiaDTO alergia = new AlergiaDTO();
        alergia.setDescripcion("Polen");
        request.setAlergia(alergia);

        assertThrows(PropuestaCambioClinicoException.class,
                () -> service.crearPropuesta(NIF_MEDICO, NIF_PACIENTE, request));
    }

    @Test
    void crearPropuesta_edicionAntecedente_guardaInstantaneaDelValorActual() {
        UUID recurso = UUID.randomUUID();
        PropuestaCambioClinicoRequestDTO request = new PropuestaCambioClinicoRequestDTO();
        request.setDominio("ANTECEDENTE");
        request.setOperacion("UPDATE");
        request.setMotivo("Actualización del diagnóstico");
        request.setIdRecursoObjetivo(recurso.toString());
        AntecedenteClinicoDTO antecedente = new AntecedenteClinicoDTO();
        antecedente.setCategoria("PERSONAL");
        antecedente.setDescripcion("Diabetes tipo 2 diagnosticada en 2016");
        request.setAntecedente(antecedente);
        when(historialClinicoService.describirRecursoHistorial(paciente.getId(), Dominio.ANTECEDENTE, recurso))
                .thenReturn("PERSONAL: Diabetes tipo 2 diagnosticada en 2015");

        PropuestaCambioClinico creada = service.crearPropuesta(NIF_MEDICO, NIF_PACIENTE, request);

        assertEquals("PERSONAL: Diabetes tipo 2 diagnosticada en 2015", creada.getDescripcionActual());
        assertEquals(recurso.toString(), creada.getIdRecursoObjetivo());
    }

    // ---- resolver ----

    private PropuestaCambioClinico propuestaPendiente(Dominio dominio, Operacion operacion, String payload) {
        PropuestaCambioClinico propuesta = new PropuestaCambioClinico();
        propuesta.setId(UUID.randomUUID());
        propuesta.setMedico(medico);
        propuesta.setPaciente(paciente);
        propuesta.setHistorialClinico(historial);
        propuesta.setDominio(dominio);
        propuesta.setOperacion(operacion);
        propuesta.setMotivo("Corrección tras revisión");
        propuesta.setPayloadJson(payload);
        propuesta.setEstado(PropuestaCambioClinico.ESTADO_PENDIENTE);
        return propuesta;
    }

    @Test
    void resolver_aceptar_aplicaElCambioYMarcaAceptada() {
        PropuestaCambioClinico propuesta = propuestaPendiente(Dominio.ANALISIS_SANGRE, Operacion.CREATE,
                "{\"label\":\"Glucosa\",\"value\":\"95\",\"unit\":\"mg/dL\"}");
        when(propuestaRepository.findById(propuesta.getId())).thenReturn(Optional.of(propuesta));

        PropuestaCambioClinico resuelta = service.resolver(NIF_PACIENTE, propuesta.getId(), true);

        assertEquals(PropuestaCambioClinico.ESTADO_ACEPTADA, resuelta.getEstado());
        verify(historialClinicoService, times(1)).aplicarCambioMedicion(
                eq(paciente.getId()), eq(medico.getId()), eq(Operacion.CREATE), isNull(),
                any(DatoClinicoEntradaDTO.class), eq("Corrección tras revisión"));
        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq(NIF_MEDICO), anyString());
        verify(auditoriaCambioService, times(1)).registrarCambio(anyString(), anyString(), eq(medico.getId().toString()),
                eq("PROPUESTA_CAMBIO_CLINICO"), anyString(), anyString(), eq(PropuestaCambioClinico.ESTADO_PENDIENTE),
                eq(PropuestaCambioClinico.ESTADO_ACEPTADA), eq(AuditoriaCambio.TipoOperacion.UPDATE), anyString());
    }

    @Test
    void resolver_rechazar_noAplicaElCambioYMarcaRechazada() {
        PropuestaCambioClinico propuesta = propuestaPendiente(Dominio.ANTECEDENTE, Operacion.DELETE, null);
        propuesta.setIdRecursoObjetivo(UUID.randomUUID().toString());
        when(propuestaRepository.findById(propuesta.getId())).thenReturn(Optional.of(propuesta));

        PropuestaCambioClinico resuelta = service.resolver(NIF_PACIENTE, propuesta.getId(), false);

        assertEquals(PropuestaCambioClinico.ESTADO_RECHAZADA, resuelta.getEstado());
        verify(historialClinicoService, never()).aplicarCambioAntecedente(any(), any(), any(), any(), any(), anyString());
        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq(NIF_MEDICO), anyString());
    }

    @Test
    void resolver_propuestaDeOtroPaciente_lanzaNoEncontrada() {
        PropuestaCambioClinico propuesta = propuestaPendiente(Dominio.ALERGIA, Operacion.CREATE,
                "{\"descripcion\":\"Polen\"}");
        when(propuestaRepository.findById(propuesta.getId())).thenReturn(Optional.of(propuesta));

        assertThrows(PropuestaCambioNoEncontradaException.class,
                () -> service.resolver(NIF_OTRO_PACIENTE, propuesta.getId(), true));
        verify(historialClinicoService, never()).aplicarCambioAlergia(any(), any(), any(), any(), any(), anyString());
    }

    @Test
    void resolver_propuestaYaResuelta_lanzaExcepcion() {
        PropuestaCambioClinico propuesta = propuestaPendiente(Dominio.ALERGIA, Operacion.CREATE, "{\"descripcion\":\"Polen\"}");
        propuesta.setEstado(PropuestaCambioClinico.ESTADO_ACEPTADA);
        when(propuestaRepository.findById(propuesta.getId())).thenReturn(Optional.of(propuesta));

        assertThrows(PropuestaCambioClinicoException.class,
                () -> service.resolver(NIF_PACIENTE, propuesta.getId(), true));
    }

    @Test
    void resolver_propuestaInexistente_lanzaNoEncontrada() {
        UUID id = UUID.randomUUID();
        when(propuestaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PropuestaCambioNoEncontradaException.class, () -> service.resolver(NIF_PACIENTE, id, true));
    }

    // ---- listar / anular ----

    @Test
    void listarPropuestasPendientesParaPaciente_devuelveLoDelRepositorio() {
        when(propuestaRepository.findByPacienteIdAndEstadoOrderByFechaCreacionDesc(
                paciente.getId(), PropuestaCambioClinico.ESTADO_PENDIENTE))
                .thenReturn(List.of(propuestaPendiente(Dominio.ANALISIS_SANGRE, Operacion.CREATE, null)));

        assertEquals(1, service.listarPropuestasPendientesParaPaciente(NIF_PACIENTE).size());
    }

    @Test
    void listarPropuestasEnviadasParaPaciente_devuelveLoDelRepositorio() {
        when(propuestaRepository.findByMedicoIdAndPacienteIdOrderByFechaCreacionDesc(medico.getId(), paciente.getId()))
                .thenReturn(List.of(propuestaPendiente(Dominio.SIGNOS_VITALES, Operacion.UPDATE, null)));

        assertEquals(1, service.listarPropuestasEnviadasParaPaciente(NIF_MEDICO, NIF_PACIENTE).size());
    }

    @Test
    void anularPendientes_marcaLasPendientesComoAnuladas() {
        PropuestaCambioClinico p1 = propuestaPendiente(Dominio.ANALISIS_SANGRE, Operacion.CREATE, null);
        PropuestaCambioClinico p2 = propuestaPendiente(Dominio.ALERGIA, Operacion.CREATE, null);
        when(propuestaRepository.findByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), PropuestaCambioClinico.ESTADO_PENDIENTE))
                .thenReturn(List.of(p1, p2));

        service.anularPendientes(medico.getId(), paciente.getId());

        assertEquals(PropuestaCambioClinico.ESTADO_ANULADA, p1.getEstado());
        assertEquals(PropuestaCambioClinico.ESTADO_ANULADA, p2.getEstado());
        verify(propuestaRepository, times(1)).saveAll(List.of(p1, p2));
    }

    @Test
    void anularPendientes_sinPendientes_noHaceNada() {
        when(propuestaRepository.findByMedicoIdAndPacienteIdAndEstado(any(), any(), anyString())).thenReturn(List.of());

        service.anularPendientes(medico.getId(), paciente.getId());

        verify(propuestaRepository, never()).saveAll(any());
    }
}
