package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hcc.tfm_hcc.converter.UsuarioExportConverter;
import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.NotificacionRepository;
import com.hcc.tfm_hcc.repository.SolicitudAsignacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.UsuarioServiceImpl;

class UsuarioServiceImplTest {

    private UsuarioMapper usuarioMapper;
    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private PerfilUsuarioService perfilUsuarioService;
    private AccessLogRepository accessLogRepository;
    private SolicitudAsignacionRepository solicitudAsignacionRepository;
    private NotificacionRepository notificacionRepository;
    private MedicoPacienteRepository medicoPacienteRepository;
    private UsuarioExportConverter usuarioExportConverter;
    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        usuarioMapper = mock(UsuarioMapper.class);
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        perfilUsuarioService = mock(PerfilUsuarioService.class);
        accessLogRepository = mock(AccessLogRepository.class);
        solicitudAsignacionRepository = mock(SolicitudAsignacionRepository.class);
        notificacionRepository = mock(NotificacionRepository.class);
        medicoPacienteRepository = mock(MedicoPacienteRepository.class);
        usuarioExportConverter = mock(UsuarioExportConverter.class);
        service = new UsuarioServiceImpl(usuarioMapper, usuarioRepository, passwordEncoder, perfilUsuarioService,
                accessLogRepository, solicitudAsignacionRepository, notificacionRepository, medicoPacienteRepository,
                usuarioExportConverter);
        when(usuarioRepository.findAll()).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String nif) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(nif, null));
    }

    private UsuarioDTO usuarioDtoValido() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif("12345678A");
        dto.setPassword("contraseñaSegura1");
        dto.setEmail("usuario@example.com");
        return dto;
    }

    // ---- altaUsuario ----

    @Test
    void altaUsuario_conDatosValidos_creaElUsuarioYAsignaElPerfilPaciente() {
        UsuarioDTO dto = usuarioDtoValido();
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("usuario@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("contraseñaSegura1")).thenReturn("hash-bcrypt");
        Usuario nuevo = new Usuario();
        nuevo.setId(UUID.randomUUID());
        when(usuarioMapper.toEntity(dto)).thenReturn(nuevo);
        when(usuarioRepository.save(nuevo)).thenReturn(nuevo);

        Usuario resultado = service.altaUsuario(dto);

        assertEquals("ACTIVO", resultado.getEstadoCuenta());
        verify(perfilUsuarioService, times(1)).asignarPerfil(nuevo.getId(), "PACIENTE");
    }

    @Test
    void altaUsuario_conDtoNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.altaUsuario(null));
    }

    @Test
    void altaUsuario_conEmailFormatoInvalido_lanzaIllegalArgumentException() {
        UsuarioDTO dto = usuarioDtoValido();
        dto.setEmail("no-es-un-email");

        assertThrows(IllegalArgumentException.class, () -> service.altaUsuario(dto));
    }

    @Test
    void altaUsuario_conNifYaExistente_lanzaIllegalArgumentException() {
        UsuarioDTO dto = usuarioDtoValido();
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(new Usuario()));

        assertThrows(IllegalArgumentException.class, () -> service.altaUsuario(dto));
    }

    @Test
    void altaUsuario_conEmailYaExistente_lanzaIllegalArgumentException() {
        UsuarioDTO dto = usuarioDtoValido();
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("usuario@example.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(IllegalArgumentException.class, () -> service.altaUsuario(dto));
    }

    // ---- getNombreUsuario ----

    @Test
    void getNombreUsuario_conApellidos_devuelveElNombreCompleto() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellido1("García");
        usuario.setApellido2("López");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));

        assertEquals("Ana García López", service.getNombreUsuario());
    }

    @Test
    void getNombreUsuario_sinAutenticar_devuelveNull() {
        when(usuarioRepository.findByNif(null)).thenReturn(Optional.empty());

        assertNull(service.getNombreUsuario());
    }

    // ---- getUsuarioActual ----

    @Test
    void getUsuarioActual_conUsuarioAutenticado_devuelveElDto() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(usuario)).thenReturn(dto);

        assertEquals(dto, service.getUsuarioActual());
    }

    @Test
    void getUsuarioActual_sinAutenticar_devuelveNull() {
        assertNull(service.getUsuarioActual());
    }

    // ---- changePassword ----

    @Test
    void changePassword_conPasswordActualCorrecta_actualizaLaContrasena() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setPassword("hash-actual");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actual123", "hash-actual")).thenReturn(true);
        when(passwordEncoder.encode("nueva456")).thenReturn("hash-nuevo");

        service.changePassword("actual123", "nueva456");

        assertEquals("hash-nuevo", usuario.getPassword());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void changePassword_conPasswordActualIncorrecta_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setPassword("hash-actual");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash-actual")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.changePassword("incorrecta", "nueva456"));
    }

    @Test
    void changePassword_sinAutenticar_lanzaIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> service.changePassword("a", "b"));
    }

    // ---- updateUsuarioActual ----

    @Test
    void updateUsuarioActual_conNombreValido_actualizaYDevuelveElDto() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        UsuarioDTO resultado = new UsuarioDTO();
        when(usuarioMapper.toDto(usuario)).thenReturn(resultado);

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNombre("NuevoNombre");

        UsuarioDTO devuelto = service.updateUsuarioActual(cambios);

        assertEquals("NuevoNombre", usuario.getNombre());
        assertEquals(resultado, devuelto);
    }

    @Test
    void updateUsuarioActual_conNombreDemasiadoLargo_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNombre("a".repeat(101));

        assertThrows(IllegalArgumentException.class, () -> service.updateUsuarioActual(cambios));
    }

    @Test
    void updateUsuarioActual_conEmailYaUsadoPorOtroUsuario_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndIdNot("ocupado@example.com", id)).thenReturn(true);

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setEmail("ocupado@example.com");

        assertThrows(IllegalArgumentException.class, () -> service.updateUsuarioActual(cambios));
    }

    @Test
    void updateUsuarioActual_conTelefonoFormatoInvalido_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setTelefono("telefono-no-valido-###");

        assertThrows(IllegalArgumentException.class, () -> service.updateUsuarioActual(cambios));
    }

    @Test
    void updateUsuarioActual_conNifYaUsadoPorOtroUsuario_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByNifAndIdNot("87654321B", id)).thenReturn(true);

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNif("87654321B");

        assertThrows(IllegalArgumentException.class, () -> service.updateUsuarioActual(cambios));
    }

    @Test
    void updateUsuarioActual_conMismoNifQueElActual_noValidaUnicidadYNoLoModifica() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(usuario)).thenReturn(new UsuarioDTO());

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNif("12345678A");

        service.updateUsuarioActual(cambios);

        verify(usuarioRepository, never()).existsByNifAndIdNot(anyString(), any());
        assertEquals("12345678A", usuario.getNif());
    }

    // ---- deleteCuentaActual ----

    @Test
    void deleteCuentaActual_anonimizaLosDatosDelUsuario() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));

        service.deleteCuentaActual();

        assertEquals("ELIMINADO", usuario.getEstadoCuenta());
        assertEquals("_eliminado_", usuario.getNombre());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    // ---- getMisLogs ----

    @Test
    void getMisLogs_conRangoDeFechas_usaElRepositorioConRango() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(id.toString());
        when(usuarioMapper.toDto(usuario)).thenReturn(dto);
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        LocalDateTime hasta = LocalDateTime.now();
        when(accessLogRepository.findByUsuarioIdAndTimestampBetweenOrderByTimestampDesc(id.toString(), desde, hasta))
                .thenReturn(List.of(new AccessLog()));
        when(usuarioExportConverter.toAccesoDto(any())).thenReturn(UserExportDTO.AccesoDTO.builder().build());

        List<UserExportDTO.AccesoDTO> resultado = service.getMisLogs(desde, hasta);

        assertEquals(1, resultado.size());
    }

    @Test
    void getMisLogs_sinRangoDeFechas_usaElRepositorioSinRango() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(id.toString());
        when(usuarioMapper.toDto(usuario)).thenReturn(dto);
        when(accessLogRepository.findByUsuarioIdOrderByTimestampDesc(id.toString())).thenReturn(List.of());

        List<UserExportDTO.AccesoDTO> resultado = service.getMisLogs(null, null);

        assertEquals(0, resultado.size());
    }

    // ---- exportUsuario ----

    @Test
    void exportUsuario_construyeElExportConLosLogsDelUsuario() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        when(usuarioRepository.findByNif("12345678A")).thenReturn(Optional.of(usuario));
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(id.toString());
        when(usuarioMapper.toDto(usuario)).thenReturn(dto);
        when(accessLogRepository.findByUsuarioIdOrderByTimestampDesc(id.toString())).thenReturn(List.of());
        UserExportDTO exportado = UserExportDTO.builder().id(id.toString()).build();
        when(usuarioExportConverter.toExportDto(eq(dto), eq(usuario), any())).thenReturn(exportado);

        assertEquals(exportado, service.exportUsuario());
    }

    // ---- listarMisSolicitudes ----

    @Test
    void listarMisSolicitudes_devuelveLasSolicitudesDelPaciente() {
        autenticarComo("12345678A");
        when(solicitudAsignacionRepository.findByPacienteNifOrderByFechaCreacionDesc("12345678A"))
                .thenReturn(List.of(new SolicitudAsignacion()));

        assertEquals(1, service.listarMisSolicitudes().size());
    }

    @Test
    void listarMisSolicitudes_sinAutenticar_lanzaIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> service.listarMisSolicitudes());
    }

    // ---- actualizarEstadoSolicitud ----

    private SolicitudAsignacion solicitudPendiente(String nifPaciente) {
        Usuario paciente = new Usuario();
        paciente.setNif(nifPaciente);
        Usuario medico = new Usuario();
        medico.setId(UUID.randomUUID());
        medico.setNif("00000000M");

        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setId(UUID.randomUUID());
        solicitud.setPaciente(paciente);
        paciente.setId(UUID.randomUUID());
        solicitud.setMedico(medico);
        solicitud.setEstado("PENDIENTE");
        return solicitud;
    }

    @Test
    void actualizarEstadoSolicitud_aceptadaSinRelacionPrevia_creaLaRelacionMedicoPaciente() {
        autenticarComo("12345678A");
        SolicitudAsignacion solicitud = solicitudPendiente("12345678A");
        when(solicitudAsignacionRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudAsignacionRepository.save(solicitud)).thenReturn(solicitud);
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteId(solicitud.getMedico().getId(), solicitud.getPaciente().getId()))
                .thenReturn(false);

        SolicitudAsignacion resultado = service.actualizarEstadoSolicitud(solicitud.getId().toString(), "ACEPTADA");

        assertEquals("ACEPTADA", resultado.getEstado());
        verify(medicoPacienteRepository, times(1)).save(any());
        verify(notificacionRepository, times(1)).save(any());
    }

    @Test
    void actualizarEstadoSolicitud_aceptadaConRelacionExistente_noCreaOtraRelacion() {
        autenticarComo("12345678A");
        SolicitudAsignacion solicitud = solicitudPendiente("12345678A");
        when(solicitudAsignacionRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudAsignacionRepository.save(solicitud)).thenReturn(solicitud);
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteId(solicitud.getMedico().getId(), solicitud.getPaciente().getId()))
                .thenReturn(true);

        service.actualizarEstadoSolicitud(solicitud.getId().toString(), "ACEPTADA");

        verify(medicoPacienteRepository, never()).save(any());
    }

    @Test
    void actualizarEstadoSolicitud_rechazada_noCreaRelacionPeroNotifica() {
        autenticarComo("12345678A");
        SolicitudAsignacion solicitud = solicitudPendiente("12345678A");
        when(solicitudAsignacionRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudAsignacionRepository.save(solicitud)).thenReturn(solicitud);

        SolicitudAsignacion resultado = service.actualizarEstadoSolicitud(solicitud.getId().toString(), "RECHAZADA");

        assertEquals("RECHAZADA", resultado.getEstado());
        verify(medicoPacienteRepository, never()).save(any());
        verify(notificacionRepository, times(1)).save(any());
    }

    @Test
    void actualizarEstadoSolicitud_conEstadoInvalido_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarEstadoSolicitud(UUID.randomUUID().toString(), "INVENTADO"));
    }

    @Test
    void actualizarEstadoSolicitud_conIdVacio_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");

        assertThrows(IllegalArgumentException.class, () -> service.actualizarEstadoSolicitud("  ", "ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_conIdFormatoInvalido_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");

        assertThrows(IllegalArgumentException.class, () -> service.actualizarEstadoSolicitud("no-es-un-uuid", "ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_conSolicitudInexistente_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        when(solicitudAsignacionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.actualizarEstadoSolicitud(id.toString(), "ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_conPacienteDistintoAlAutenticado_lanzaIllegalStateException() {
        autenticarComo("otro-nif");
        SolicitudAsignacion solicitud = solicitudPendiente("12345678A");
        when(solicitudAsignacionRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));

        assertThrows(IllegalStateException.class,
                () -> service.actualizarEstadoSolicitud(solicitud.getId().toString(), "ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_sinAutenticar_lanzaIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> service.actualizarEstadoSolicitud(UUID.randomUUID().toString(), "ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_conFalloAlNotificar_noPropagaLaExcepcion() {
        autenticarComo("12345678A");
        SolicitudAsignacion solicitud = solicitudPendiente("12345678A");
        when(solicitudAsignacionRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudAsignacionRepository.save(solicitud)).thenReturn(solicitud);
        when(notificacionRepository.save(any())).thenThrow(new RuntimeException("fallo de BD"));

        SolicitudAsignacion resultado = service.actualizarEstadoSolicitud(solicitud.getId().toString(), "RECHAZADA");

        assertEquals("RECHAZADA", resultado.getEstado());
    }
}
