package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

import com.hcc.tfm_hcc.exception.RelacionMedicoPacienteNoEncontradaException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService.IniciadorRevocacion;
import com.hcc.tfm_hcc.service.impl.RelacionMedicoPacienteServiceImpl;

class RelacionMedicoPacienteServiceImplTest {

    private static final String NIF_MEDICO = "11111111A";
    private static final String NIF_PACIENTE = "22222222B";

    private MedicoPacienteRepository medicoPacienteRepository;
    private UsuarioRepository usuarioRepository;
    private HmacSearchIndexService hmacSearchIndexService;
    private AuditoriaCambioService auditoriaCambioService;
    private RelacionMedicoPacienteServiceImpl service;

    private Usuario medico;
    private Usuario paciente;

    @BeforeEach
    void setUp() {
        medicoPacienteRepository = mock(MedicoPacienteRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        auditoriaCambioService = mock(AuditoriaCambioService.class);
        when(hmacSearchIndexService.indexar(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0, String.class));
        when(medicoPacienteRepository.save(any(MedicoPaciente.class))).thenAnswer(inv -> inv.getArgument(0));

        service = new RelacionMedicoPacienteServiceImpl(medicoPacienteRepository, usuarioRepository,
                hmacSearchIndexService, auditoriaCambioService);

        medico = usuarioConNif(NIF_MEDICO);
        medico.setNombre("Ana");
        paciente = usuarioConNif(NIF_PACIENTE);

        when(usuarioRepository.findByNifHash("hash-" + NIF_MEDICO)).thenReturn(Optional.of(medico));
        when(usuarioRepository.findByNifHash("hash-" + NIF_PACIENTE)).thenReturn(Optional.of(paciente));
    }

    private Usuario usuarioConNif(String nif) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif(nif);
        usuario.setNifHash("hash-" + nif);
        return usuario;
    }

    private MedicoPaciente relacion(String estado) {
        MedicoPaciente relacion = new MedicoPaciente();
        relacion.setId(UUID.randomUUID());
        relacion.setMedico(medico);
        relacion.setPaciente(paciente);
        relacion.setEstado(estado);
        return relacion;
    }

    // ---- listarMedicosActivos ----

    @Test
    void listarMedicosActivos_devuelveLosMedicosDeLasRelacionesActivas() {
        when(medicoPacienteRepository.findByPacienteIdAndEstado(paciente.getId(), MedicoPaciente.ESTADO_ACTIVA))
                .thenReturn(List.of(relacion(MedicoPaciente.ESTADO_ACTIVA)));

        List<Usuario> medicos = service.listarMedicosActivos(NIF_PACIENTE);

        assertEquals(1, medicos.size());
        assertEquals(medico, medicos.get(0));
    }

    @Test
    void listarMedicosActivos_conPacienteInexistente_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.findByNifHash("hash-" + NIF_PACIENTE)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.listarMedicosActivos(NIF_PACIENTE));
    }

    // ---- revocarRelacion ----

    @Test
    void revocarRelacion_conRelacionActiva_laMarcaRevocadaYAudita() {
        MedicoPaciente activa = relacion(MedicoPaciente.ESTADO_ACTIVA);
        when(medicoPacienteRepository.findByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)).thenReturn(List.of(activa));

        MedicoPaciente resultado = service.revocarRelacion(NIF_MEDICO, NIF_PACIENTE, IniciadorRevocacion.PACIENTE);

        assertEquals(MedicoPaciente.ESTADO_REVOCADA, resultado.getEstado());
        verify(medicoPacienteRepository, times(1)).save(activa);
        verify(auditoriaCambioService, times(1)).registrarCambio(
                anyString(), anyString(), anyString(), eq("REVOCACION_RELACION"), eq("medico_paciente"),
                anyString(), eq(MedicoPaciente.ESTADO_ACTIVA), eq(MedicoPaciente.ESTADO_REVOCADA),
                eq(AuditoriaCambio.TipoOperacion.UPDATE), anyString());
    }

    @Test
    void revocarRelacion_sinRelacionActiva_lanzaRelacionMedicoPacienteNoEncontradaException() {
        when(medicoPacienteRepository.findByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)).thenReturn(List.of());

        assertThrows(RelacionMedicoPacienteNoEncontradaException.class,
                () -> service.revocarRelacion(NIF_MEDICO, NIF_PACIENTE, IniciadorRevocacion.MEDICO));
        verify(medicoPacienteRepository, never()).save(any());
    }

    @Test
    void revocarRelacion_conMedicoInexistente_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.findByNifHash("hash-" + NIF_MEDICO)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> service.revocarRelacion(NIF_MEDICO, NIF_PACIENTE, IniciadorRevocacion.MEDICO));
    }

    // ---- verificarAccesoMedicoActivo ----

    @Test
    void verificarAccesoMedicoActivo_conRelacionActiva_devuelveElPaciente() {
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)).thenReturn(true);

        assertEquals(paciente, service.verificarAccesoMedicoActivo(NIF_MEDICO, NIF_PACIENTE));
    }

    @Test
    void verificarAccesoMedicoActivo_sinRelacionActiva_lanzaUsuarioSinPermisoException() {
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)).thenReturn(false);

        assertThrows(UsuarioSinPermisoException.class,
                () -> service.verificarAccesoMedicoActivo(NIF_MEDICO, NIF_PACIENTE));
    }

    @Test
    void verificarAccesoMedicoActivo_conPacienteConTratamientoLimitado_lanzaUsuarioSinPermisoException() {
        paciente.setEstadoCuenta(Usuario.ESTADO_CUENTA_SUSPENDIDO);

        assertThrows(UsuarioSinPermisoException.class,
                () -> service.verificarAccesoMedicoActivo(NIF_MEDICO, NIF_PACIENTE));
    }

    @Test
    void verificarAccesoMedicoActivo_conPacienteInexistente_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.findByNifHash("hash-" + NIF_PACIENTE)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class,
                () -> service.verificarAccesoMedicoActivo(NIF_MEDICO, NIF_PACIENTE));
    }

    @Test
    void revocarRelacion_iniciadaPorElMedico_registraAlMedicoComoAutorDelCambio() {
        MedicoPaciente activa = relacion(MedicoPaciente.ESTADO_ACTIVA);
        when(medicoPacienteRepository.findByMedicoIdAndPacienteIdAndEstado(
                medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)).thenReturn(List.of(activa));

        service.revocarRelacion(NIF_MEDICO, NIF_PACIENTE, IniciadorRevocacion.MEDICO);

        verify(auditoriaCambioService).registrarCambio(
                eq(medico.getId().toString()), eq(paciente.getId().toString()), eq(medico.getId().toString()),
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), anyString());
    }
}
