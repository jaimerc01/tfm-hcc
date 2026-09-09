package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.hcc.tfm_hcc.converter.PacienteConverter;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.MedicoServiceImpl;
import com.hcc.tfm_hcc.service.PropuestaCambioClinicoService;

class MedicoServiceImplTest {

    private NotificacionFacade notificacionFacade;
    private UsuarioRepository usuarioRepository;
    private PerfilRepository perfilRepository;
    private MedicoPacienteRepository medicoPacienteRepository;
    private UsuarioMapper usuarioMapper;
    private UsuarioFacade usuarioFacade;
    private PacienteConverter pacienteConverter;
    private PerfilUsuarioService perfilUsuarioService;
    private HmacSearchIndexService hmacSearchIndexService;
    private PropuestaCambioClinicoService propuestaCambioClinicoService;
    private MedicoServiceImpl service;

    @BeforeEach
    void setUp() {
        notificacionFacade = mock(NotificacionFacade.class);
        usuarioRepository = mock(UsuarioRepository.class);
        perfilRepository = mock(PerfilRepository.class);
        medicoPacienteRepository = mock(MedicoPacienteRepository.class);
        usuarioMapper = mock(UsuarioMapper.class);
        usuarioFacade = mock(UsuarioFacade.class);
        pacienteConverter = mock(PacienteConverter.class);
        perfilUsuarioService = mock(PerfilUsuarioService.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        propuestaCambioClinicoService = mock(PropuestaCambioClinicoService.class);
        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        when(hmacSearchIndexService.indexar("00000000Z")).thenReturn("hash-00000000Z");
        service = new MedicoServiceImpl(notificacionFacade, usuarioRepository, perfilRepository,
                medicoPacienteRepository, usuarioMapper, usuarioFacade, pacienteConverter, perfilUsuarioService,
                hmacSearchIndexService, propuestaCambioClinicoService);
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conFechaCoincidente_devuelveElPaciente() {
        Usuario usuario = new Usuario();
        usuario.setFechaNacimiento(LocalDateTime.of(1990, 5, 20, 0, 0));
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        PacienteDTO dto = new PacienteDTO();
        when(pacienteConverter.toDto(usuario)).thenReturn(dto);

        PacienteDTO resultado = service.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20");

        assertEquals(dto, resultado);
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conFechaDistinta_devuelveNull() {
        Usuario usuario = new Usuario();
        usuario.setFechaNacimiento(LocalDateTime.of(1990, 5, 20, 0, 0));
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertNull(service.buscarPacientePorDniYFechaNacimiento("12345678A", "2000-01-01"));
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conUsuarioInexistente_devuelveNull() {
        when(usuarioRepository.findByNifHash("hash-00000000Z")).thenReturn(Optional.empty());

        assertNull(service.buscarPacientePorDniYFechaNacimiento("00000000Z", "1990-05-20"));
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conDniVacio_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class,
                () -> service.buscarPacientePorDniYFechaNacimiento("  ", "1990-05-20"));
    }

    @Test
    void buscarPacientePorDniYFechaNacimiento_conFechaNula_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class,
                () -> service.buscarPacientePorDniYFechaNacimiento("12345678A", null));
    }

    @Test
    void listarMedicos_devuelveLosUsuariosConPerfilMedico() {
        Usuario medico = new Usuario();
        UsuarioDTO dto = new UsuarioDTO();
        when(perfilUsuarioService.listarUsuariosPorRol("MEDICO")).thenReturn(List.of(medico));
        when(usuarioMapper.toDto(medico)).thenReturn(dto);

        List<UsuarioDTO> resultado = service.listarMedicos();

        assertEquals(1, resultado.size());
    }

    @Test
    void listarMedicos_conErrorEnElRepositorio_lanzaMedicoOperacionException() {
        when(perfilUsuarioService.listarUsuariosPorRol("MEDICO")).thenThrow(new RuntimeException("fallo"));

        assertThrows(MedicoOperacionException.class, () -> service.listarMedicos());
    }

    @Test
    void crearMedico_conDatosValidos_creaElUsuarioYAsignaElPerfil() {
        UsuarioDTO medicoDTO = new UsuarioDTO();
        medicoDTO.setNif("12345678A");
        UUID nuevoId = UUID.randomUUID();
        UsuarioDTO creado = new UsuarioDTO();
        creado.setId(nuevoId.toString());
        when(usuarioFacade.altaUsuario(medicoDTO)).thenReturn(creado);

        UsuarioDTO resultado = service.crearMedico(medicoDTO);

        assertEquals(creado, resultado);
        assertEquals("ACTIVO", medicoDTO.getEstadoCuenta());
        verify(perfilUsuarioService, times(1)).asignarPerfil(nuevoId, "MEDICO");
    }

    @Test
    void crearMedico_conDtoNulo_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class, () -> service.crearMedico(null));
    }

    @Test
    void crearMedico_conNifVacio_lanzaMedicoValidationException() {
        UsuarioDTO medicoDTO = new UsuarioDTO();
        medicoDTO.setNif("  ");

        assertThrows(MedicoValidationException.class, () -> service.crearMedico(medicoDTO));
    }

    @Test
    void crearMedico_conFalloEnAltaUsuario_lanzaMedicoOperacionException() {
        UsuarioDTO medicoDTO = new UsuarioDTO();
        medicoDTO.setNif("12345678A");
        when(usuarioFacade.altaUsuario(medicoDTO)).thenThrow(new RuntimeException("fallo de BD"));

        assertThrows(MedicoOperacionException.class, () -> service.crearMedico(medicoDTO));
    }

    @Test
    void actualizarMedico_conMedicoExistente_actualizaSoloLosCamposProporcionados() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setNombre("NombreViejo");
        usuario.setEmail("viejo@example.com");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        UsuarioDTO resultado = new UsuarioDTO();
        when(usuarioMapper.toDto(usuario)).thenReturn(resultado);

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNombre("NombreNuevo");

        UsuarioDTO devuelto = service.actualizarMedico(id, cambios);

        assertEquals("NombreNuevo", usuario.getNombre());
        assertEquals("viejo@example.com", usuario.getEmail());
        assertEquals(resultado, devuelto);
    }

    @Test
    void actualizarMedico_conMedicoInexistente_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.actualizarMedico(id, new UsuarioDTO()));
    }

    @Test
    void eliminarMedico_conMedicoExistenteYPacientesAsociados_notificaYMarcaEliminado() {
        UUID id = UUID.randomUUID();
        Usuario medico = new Usuario();
        medico.setNombre("Dr. Ejemplo");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(medico));
        when(perfilUsuarioService.tienePerfil(id, "MEDICO")).thenReturn(true);

        Usuario paciente = new Usuario();
        paciente.setNif("87654321B");
        MedicoPaciente relacion = new MedicoPaciente();
        relacion.setPaciente(paciente);
        when(medicoPacienteRepository.findByMedicoIdAndEstadoNot(id, "REVOCADA")).thenReturn(List.of(relacion));

        UUID resultado = service.eliminarMedico(id);

        assertEquals(id, resultado);
        assertEquals("ELIMINADO", medico.getEstadoCuenta());
        assertNotNull(medico.getFechaEliminacion());
        assertEquals("REVOCADA", relacion.getEstado());
        verify(medicoPacienteRepository, times(1)).saveAll(List.of(relacion));
        verify(notificacionFacade, times(1)).crearNotificacionParaUsuario(eq("87654321B"), anyString());
        verify(perfilUsuarioService, times(1)).revocarPerfil(id, "MEDICO");
        verify(usuarioRepository, times(1)).save(medico);
    }

    @Test
    void eliminarMedico_conIdNulo_lanzaMedicoValidationException() {
        assertThrows(MedicoValidationException.class, () -> service.eliminarMedico(null));
    }

    @Test
    void eliminarMedico_conMedicoInexistente_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.eliminarMedico(id));
    }

    @Test
    void eliminarMedico_conUsuarioSinPerfilMedico_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(new Usuario()));
        when(perfilUsuarioService.tienePerfil(id, "MEDICO")).thenReturn(false);

        assertThrows(UsuarioNoEncontradoException.class, () -> service.eliminarMedico(id));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void setPerfilMedico_conAsignarTrue_asignaElPerfilMedico() {
        UUID id = UUID.randomUUID();
        when(perfilRepository.getPerfilByRol("MEDICO")).thenReturn(Optional.of(new Perfil()));
        when(perfilRepository.getPerfilByRol("PACIENTE")).thenReturn(Optional.of(new Perfil()));
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(new Usuario()));

        UUID resultado = service.setPerfilMedico(id, true);

        assertEquals(id, resultado);
        verify(perfilUsuarioService, times(1)).asignarPerfil(id, "MEDICO");
    }

    @Test
    void setPerfilMedico_conAsignarFalse_revocaYGarantizaPerfilPaciente() {
        UUID id = UUID.randomUUID();
        Usuario medico = new Usuario();
        medico.setNombre("Dr. Ejemplo");
        when(perfilRepository.getPerfilByRol("MEDICO")).thenReturn(Optional.of(new Perfil()));
        when(perfilRepository.getPerfilByRol("PACIENTE")).thenReturn(Optional.of(new Perfil()));
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(medico));
        when(perfilUsuarioService.tienePerfil(id, "MEDICO")).thenReturn(true);

        Usuario paciente = new Usuario();
        paciente.setNif("87654321B");
        MedicoPaciente relacion = new MedicoPaciente();
        relacion.setPaciente(paciente);
        when(medicoPacienteRepository.findByMedicoIdAndEstadoNot(id, "REVOCADA")).thenReturn(List.of(relacion));
        when(perfilUsuarioService.tienePerfil(id, "PACIENTE")).thenReturn(false);

        UUID resultado = service.setPerfilMedico(id, false);

        assertEquals(id, resultado);
        assertEquals("REVOCADA", relacion.getEstado());
        verify(medicoPacienteRepository, times(1)).saveAll(List.of(relacion));
        verify(perfilUsuarioService, times(1)).revocarPerfil(id, "MEDICO");
        verify(perfilUsuarioService, times(1)).asignarPerfil(id, "PACIENTE");
    }

    @Test
    void setPerfilMedico_conAsignarFalseYUsuarioSinPerfilMedico_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(perfilRepository.getPerfilByRol("MEDICO")).thenReturn(Optional.of(new Perfil()));
        when(perfilRepository.getPerfilByRol("PACIENTE")).thenReturn(Optional.of(new Perfil()));
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(new Usuario()));
        when(perfilUsuarioService.tienePerfil(id, "MEDICO")).thenReturn(false);

        assertThrows(UsuarioNoEncontradoException.class, () -> service.setPerfilMedico(id, false));
        verify(perfilUsuarioService, never()).revocarPerfil(any(), anyString());
    }

    @Test
    void setPerfilMedico_conPerfilesNoConfigurados_lanzaPerfilNotFoundException() {
        UUID id = UUID.randomUUID();
        when(perfilRepository.getPerfilByRol("MEDICO")).thenReturn(Optional.empty());

        assertThrows(PerfilNotFoundException.class, () -> service.setPerfilMedico(id, true));
    }
}
