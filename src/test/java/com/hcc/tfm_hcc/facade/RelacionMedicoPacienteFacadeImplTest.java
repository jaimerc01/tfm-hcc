package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hcc.tfm_hcc.converter.MedicoResumenConverter;
import com.hcc.tfm_hcc.dto.MedicoResumenDTO;
import com.hcc.tfm_hcc.exception.RelacionMedicoPacienteNoEncontradaException;
import com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException;
import com.hcc.tfm_hcc.facade.impl.RelacionMedicoPacienteFacadeImpl;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.PropuestaCambioClinicoService;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService.IniciadorRevocacion;

class RelacionMedicoPacienteFacadeImplTest {

    private RelacionMedicoPacienteService relacionMedicoPacienteService;
    private PropuestaCambioClinicoService propuestaCambioClinicoService;
    private NotificacionFacade notificacionFacade;
    private RelacionMedicoPacienteFacadeImpl facade;

    @BeforeEach
    void setUp() {
        relacionMedicoPacienteService = mock(RelacionMedicoPacienteService.class);
        propuestaCambioClinicoService = mock(PropuestaCambioClinicoService.class);
        notificacionFacade = mock(NotificacionFacade.class);
        facade = new RelacionMedicoPacienteFacadeImpl(relacionMedicoPacienteService, propuestaCambioClinicoService,
                notificacionFacade, new MedicoResumenConverter());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String nif) {
        Usuario usuario = new Usuario();
        usuario.setNif(nif);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null));
    }

    private Usuario usuario(String nif, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setNif(nif);
        usuario.setNombre(nombre);
        return usuario;
    }

    private MedicoPaciente relacion(Usuario medico, Usuario paciente) {
        MedicoPaciente relacion = new MedicoPaciente();
        relacion.setMedico(medico);
        relacion.setPaciente(paciente);
        relacion.setEstado(MedicoPaciente.ESTADO_REVOCADA);
        return relacion;
    }

    // ---- listarMisMedicos ----

    @Test
    void listarMisMedicos_convierteLosMedicosDelServicio() {
        autenticarComo("22222222B");
        when(relacionMedicoPacienteService.listarMedicosActivos("22222222B"))
                .thenReturn(List.of(usuario("11111111A", "Ana"), usuario("33333333C", "Bea")));

        List<MedicoResumenDTO> medicos = facade.listarMisMedicos();

        assertEquals(2, medicos.size());
        assertEquals("11111111A", medicos.get(0).getNif());
    }

    @Test
    void listarMisMedicos_sinUsuarioAutenticado_lanzaUsuarioNoAutenticadoException() {
        assertThrows(UsuarioNoAutenticadoException.class, () -> facade.listarMisMedicos());
    }

    // ---- desasignarMiMedico (paciente) ----

    @Test
    void desasignarMiMedico_revocaComoPacienteYNotificaAlMedico() {
        autenticarComo("22222222B");
        Usuario medico = usuario("11111111A", "Ana");
        Usuario paciente = usuario("22222222B", "Carlos");
        when(relacionMedicoPacienteService.revocarRelacion("11111111A", "22222222B", IniciadorRevocacion.PACIENTE))
                .thenReturn(relacion(medico, paciente));

        facade.desasignarMiMedico("11111111A");

        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq("11111111A"), anyString());
    }

    @Test
    void desasignarMiMedico_sinRelacionActiva_propagaLaExcepcion() {
        autenticarComo("22222222B");
        when(relacionMedicoPacienteService.revocarRelacion(anyString(), anyString(), eq(IniciadorRevocacion.PACIENTE)))
                .thenThrow(new RelacionMedicoPacienteNoEncontradaException("no activa"));

        assertThrows(RelacionMedicoPacienteNoEncontradaException.class, () -> facade.desasignarMiMedico("11111111A"));
        verify(notificacionFacade, never()).crearNotificacionParaUsuario(anyString(), anyString());
    }

    @Test
    void desasignarMiMedico_conFalloAlNotificar_noPropagaLaExcepcion() {
        autenticarComo("22222222B");
        Usuario medico = usuario("11111111A", "Ana");
        when(relacionMedicoPacienteService.revocarRelacion("11111111A", "22222222B", IniciadorRevocacion.PACIENTE))
                .thenReturn(relacion(medico, usuario("22222222B", "Carlos")));
        when(notificacionFacade.crearNotificacionParaUsuario(anyString(), anyString()))
                .thenThrow(new RuntimeException("fallo"));

        facade.desasignarMiMedico("11111111A");
    }

    // ---- desasignarMiPaciente (médico) ----

    @Test
    void desasignarMiPaciente_revocaComoMedicoYNotificaAlPaciente() {
        autenticarComo("11111111A");
        Usuario medico = usuario("11111111A", "Ana");
        Usuario paciente = usuario("22222222B", "Carlos");
        when(relacionMedicoPacienteService.revocarRelacion("11111111A", "22222222B", IniciadorRevocacion.MEDICO))
                .thenReturn(relacion(medico, paciente));

        facade.desasignarMiPaciente("22222222B");

        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq("22222222B"), anyString());
    }

    @Test
    void desasignarMiPaciente_sinUsuarioAutenticado_lanzaUsuarioNoAutenticadoException() {
        assertThrows(UsuarioNoAutenticadoException.class, () -> facade.desasignarMiPaciente("22222222B"));
    }
}
