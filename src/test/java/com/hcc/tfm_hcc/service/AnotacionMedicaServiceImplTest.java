package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AnotacionMedicaRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.AnotacionMedicaServiceImpl;

class AnotacionMedicaServiceImplTest {

    private static final String NIF_MEDICO = "11111111A";
    private static final String NIF_PACIENTE = "22222222B";

    private AnotacionMedicaRepository anotacionMedicaRepository;
    private UsuarioRepository usuarioRepository;
    private MedicoPacienteRepository medicoPacienteRepository;
    private HmacSearchIndexService hmacSearchIndexService;
    private AuditoriaCambioService auditoriaCambioService;
    private AnotacionMedicaServiceImpl service;

    private Usuario medico;
    private Usuario paciente;

    @BeforeEach
    void setUp() {
        anotacionMedicaRepository = mock(AnotacionMedicaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        medicoPacienteRepository = mock(MedicoPacienteRepository.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        auditoriaCambioService = mock(AuditoriaCambioService.class);
        when(hmacSearchIndexService.indexar(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0, String.class));

        service = new AnotacionMedicaServiceImpl(anotacionMedicaRepository, usuarioRepository, medicoPacienteRepository,
                hmacSearchIndexService, auditoriaCambioService);

        medico = usuarioConNif(NIF_MEDICO);
        medico.setNombre("Ana");
        paciente = usuarioConNif(NIF_PACIENTE);

        when(usuarioRepository.findByNifHash("hash-" + NIF_MEDICO)).thenReturn(Optional.of(medico));
        when(usuarioRepository.findByNifHash("hash-" + NIF_PACIENTE)).thenReturn(Optional.of(paciente));
        // Al persistir, JPA (GenerationType.UUID) asigna el identificador antes del insert.
        when(anotacionMedicaRepository.save(any(AnotacionMedica.class))).thenAnswer(inv -> {
            AnotacionMedica guardada = inv.getArgument(0);
            if (guardada.getId() == null) {
                guardada.setId(UUID.randomUUID());
            }
            return guardada;
        });
    }

    private Usuario usuarioConNif(String nif) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif(nif);
        usuario.setNifHash("hash-" + nif);
        return usuario;
    }

    private void conRelacionActiva() {
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)).thenReturn(true);
    }

    // ---- crearAnotacion ----

    @Test
    void crearAnotacion_conRelacionActiva_persisteNotificaYAudita() {
        conRelacionActiva();

        AnotacionMedica creada = service.crearAnotacion(NIF_MEDICO, NIF_PACIENTE, "  Revisar tensión  ");

        assertEquals("Revisar tensión", creada.getMensaje());
        assertEquals(medico, creada.getMedico());
        assertEquals(paciente, creada.getPaciente());
        verify(anotacionMedicaRepository, times(1)).save(any(AnotacionMedica.class));
        verify(auditoriaCambioService, times(1)).registrarCambio(
                anyString(), anyString(), anyString(), eq("ANOTACION_MEDICA"), eq("anotacion_medica"),
                anyString(), anyString(), anyString(), eq(AuditoriaCambio.TipoOperacion.CREATE), anyString());
    }

    @Test
    void crearAnotacion_conMensajeNulo_lanzaMedicoValidationExceptionYNoPersiste() {
        assertThrows(MedicoValidationException.class, () -> service.crearAnotacion(NIF_MEDICO, NIF_PACIENTE, null));
        verify(anotacionMedicaRepository, never()).save(any());
    }

    @Test
    void crearAnotacion_conMensajeSoloEspacios_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class, () -> service.crearAnotacion(NIF_MEDICO, NIF_PACIENTE, "   "));
    }

    @Test
    void crearAnotacion_conPacienteInexistente_lanzaIllegalArgumentException() {
        when(usuarioRepository.findByNifHash("hash-" + NIF_PACIENTE)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.crearAnotacion(NIF_MEDICO, NIF_PACIENTE, "texto"));
    }

    @Test
    void crearAnotacion_conMedicoInexistente_lanzaIllegalStateException() {
        when(usuarioRepository.findByNifHash("hash-" + NIF_MEDICO)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.crearAnotacion(NIF_MEDICO, NIF_PACIENTE, "texto"));
    }

    @Test
    void crearAnotacion_conPacienteConTratamientoLimitado_lanzaIllegalStateException() {
        paciente.setEstadoCuenta(Usuario.ESTADO_CUENTA_SUSPENDIDO);
        conRelacionActiva();

        assertThrows(IllegalStateException.class,
                () -> service.crearAnotacion(NIF_MEDICO, NIF_PACIENTE, "texto"));
        verify(anotacionMedicaRepository, never()).save(any());
    }

    @Test
    void crearAnotacion_sinRelacionActiva_lanzaIllegalStateException() {
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> service.crearAnotacion(NIF_MEDICO, NIF_PACIENTE, "texto"));
        verify(anotacionMedicaRepository, never()).save(any());
    }

    // ---- listarAnotacionesPaciente ----

    @Test
    void listarAnotacionesPaciente_sinFiltros_devuelveLoDelRepositorio() {
        when(anotacionMedicaRepository.findByPacienteIdOrderByFechaCreacionDesc(paciente.getId()))
                .thenReturn(List.of(anotacionEn(LocalDateTime.now()), anotacionEn(LocalDateTime.now().minusDays(1))));

        List<AnotacionMedica> resultado = service.listarAnotacionesPaciente(NIF_PACIENTE, null, null, null);

        assertEquals(2, resultado.size());
    }

    @Test
    void listarAnotacionesPaciente_conFiltroDeMedico_usaLaConsultaPorMedico() {
        Usuario medicoFiltro = usuarioConNif("33333333C");
        when(usuarioRepository.findByNifHash("hash-33333333C")).thenReturn(Optional.of(medicoFiltro));
        when(anotacionMedicaRepository.findByPacienteIdAndMedicoIdOrderByFechaCreacionDesc(paciente.getId(), medicoFiltro.getId()))
                .thenReturn(List.of(anotacionEn(LocalDateTime.now())));

        List<AnotacionMedica> resultado = service.listarAnotacionesPaciente(NIF_PACIENTE, "33333333C", null, null);

        assertEquals(1, resultado.size());
        verify(anotacionMedicaRepository, never()).findByPacienteIdOrderByFechaCreacionDesc(any());
    }

    @Test
    void listarAnotacionesPaciente_conFiltroDeMedicoInexistente_devuelveListaVacia() {
        when(usuarioRepository.findByNifHash("hash-99999999X")).thenReturn(Optional.empty());

        assertTrue(service.listarAnotacionesPaciente(NIF_PACIENTE, "99999999X", null, null).isEmpty());
    }

    @Test
    void listarAnotacionesPaciente_conRangoDeFechas_descartaLasQueQuedanFuera() {
        LocalDateTime ahora = LocalDateTime.now();
        when(anotacionMedicaRepository.findByPacienteIdOrderByFechaCreacionDesc(paciente.getId()))
                .thenReturn(List.of(
                        anotacionEn(ahora),
                        anotacionEn(ahora.minusDays(10)),
                        anotacionEn(ahora.minusDays(30))));

        List<AnotacionMedica> resultado = service.listarAnotacionesPaciente(
                NIF_PACIENTE, null, ahora.minusDays(15), ahora.plusDays(1));

        assertEquals(2, resultado.size());
    }

    @Test
    void listarAnotacionesPaciente_conPacienteInexistente_lanzaIllegalArgumentException() {
        when(usuarioRepository.findByNifHash("hash-" + NIF_PACIENTE)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.listarAnotacionesPaciente(NIF_PACIENTE, null, null, null));
    }

    private AnotacionMedica anotacionEn(LocalDateTime fecha) {
        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setId(UUID.randomUUID());
        anotacion.setMedico(medico);
        anotacion.setPaciente(paciente);
        anotacion.setMensaje("texto");
        anotacion.setFechaCreacion(fecha);
        return anotacion;
    }
}
