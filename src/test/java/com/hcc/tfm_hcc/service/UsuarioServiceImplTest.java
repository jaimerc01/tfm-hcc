package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.hcc.tfm_hcc.exception.ReautenticacionRequeridaException;
import com.hcc.tfm_hcc.dto.TotpSetupResponseDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.MedicoPaciente;
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
    private HmacSearchIndexService hmacSearchIndexService;
    private TotpService totpService;
    private AuditoriaCambioService auditoriaCambioService;
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
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        when(hmacSearchIndexService.indexar(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0, String.class));
        totpService = mock(TotpService.class);
        auditoriaCambioService = mock(AuditoriaCambioService.class);
        service = new UsuarioServiceImpl(usuarioMapper, usuarioRepository, passwordEncoder, perfilUsuarioService,
                accessLogRepository, solicitudAsignacionRepository, notificacionRepository, medicoPacienteRepository,
                usuarioExportConverter, hmacSearchIndexService, totpService, auditoriaCambioService);
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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailHash("hash-usuario@example.com")).thenReturn(Optional.empty());
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
    void altaUsuario_registraElConsentimientoEnLaCuentaYEnLaAuditoria() {
        org.springframework.test.util.ReflectionTestUtils.setField(service, "versionPoliticaPrivacidad", "2026-09-03");
        UsuarioDTO dto = usuarioDtoValido();
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailHash("hash-usuario@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("contraseñaSegura1")).thenReturn("hash-bcrypt");
        Usuario nuevo = new Usuario();
        when(usuarioMapper.toEntity(dto)).thenReturn(nuevo);
        when(usuarioRepository.save(nuevo)).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        service.altaUsuario(dto);

        assertNotNull(nuevo.getFechaConsentimiento());
        assertEquals("2026-09-03", nuevo.getVersionPoliticaPrivacidad());
        verify(auditoriaCambioService, times(1)).registrarCambio(
                anyString(), anyString(), isNull(), eq("CONSENTIMIENTO"), eq("usuario"),
                anyString(), anyString(), anyString(),
                eq(com.hcc.tfm_hcc.model.AuditoriaCambio.TipoOperacion.CREATE), anyString());
    }

    /**
     * Regresión: si el UsuarioDTO llegase con un id ya fijado (p. ej. porque algún
     * futuro llamador reutilizara un DTO con datos de un usuario existente), JPA
     * trataría un id no nulo como "entidad existente" y el guardado sería una
     * actualización de esa fila en vez de una inserción nueva -- exactamente lo que
     * permitiría sobrescribir una cuenta ajena. altaUsuario debe anular siempre el id
     * antes de guardar, sin importar lo que traiga el DTO de entrada.
     */
    @Test
    void altaUsuario_anulaElIdAunqueElMapperLoHayaCopiadoDelDto() {
        UsuarioDTO dto = usuarioDtoValido();
        dto.setId(UUID.randomUUID().toString());
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailHash("hash-usuario@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("contraseñaSegura1")).thenReturn("hash-bcrypt");
        Usuario nuevo = new Usuario();
        nuevo.setId(UUID.randomUUID()); // simula que el mapper hubiera copiado el id del dto
        when(usuarioMapper.toEntity(dto)).thenReturn(nuevo);
        when(usuarioRepository.save(nuevo)).thenReturn(nuevo);

        service.altaUsuario(dto);

        assertNull(nuevo.getId());
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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(new Usuario()));

        assertThrows(IllegalArgumentException.class, () -> service.altaUsuario(dto));
    }

    @Test
    void altaUsuario_conEmailYaExistente_lanzaIllegalArgumentException() {
        UsuarioDTO dto = usuarioDtoValido();
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailHash("hash-usuario@example.com")).thenReturn(Optional.of(new Usuario()));

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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertEquals("Ana García López", service.getNombreUsuario());
    }

    @Test
    void getNombreUsuario_sinAutenticar_devuelveNull() {
        assertNull(service.getNombreUsuario());
    }

    // ---- getUsuarioActual ----

    @Test
    void getUsuarioActual_conUsuarioAutenticado_devuelveElDto() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailHashAndIdNot("hash-ocupado@example.com", id)).thenReturn(true);

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setEmail("ocupado@example.com");

        assertThrows(IllegalArgumentException.class, () -> service.updateUsuarioActual(cambios));
    }

    @Test
    void updateUsuarioActual_conTelefonoFormatoInvalido_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByNifHashAndIdNot("hash-87654321B", id)).thenReturn(true);

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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(usuario)).thenReturn(new UsuarioDTO());

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNif("12345678A");

        service.updateUsuarioActual(cambios);

        verify(usuarioRepository, never()).existsByNifHashAndIdNot(anyString(), any());
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
        usuario.setPassword("hash-actual");
        usuario.setFechaNacimiento(LocalDateTime.of(1990, 5, 14, 0, 0));
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("contraseñaActual", "hash-actual")).thenReturn(true);

        service.deleteCuentaActual("contraseñaActual");

        assertEquals("ELIMINADO", usuario.getEstadoCuenta());
        assertEquals("_eliminado_", usuario.getNombre());
        assertNull(usuario.getFechaNacimiento());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void deleteCuentaActual_sinContrasena_lanzaReautenticacionRequerida() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif("12345678A");
        usuario.setPassword("hash-actual");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertThrows(ReautenticacionRequeridaException.class, () -> service.deleteCuentaActual(null));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deleteCuentaActual_conContrasenaIncorrecta_lanzaReautenticacionRequerida() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif("12345678A");
        usuario.setPassword("hash-actual");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash-actual")).thenReturn(false);

        assertThrows(ReautenticacionRequeridaException.class, () -> service.deleteCuentaActual("incorrecta"));
        verify(usuarioRepository, never()).save(any());
    }

    // ---- getMisLogs ----

    @Test
    void getMisLogs_conRangoDeFechas_usaElRepositorioConRango() {
        autenticarComo("12345678A");
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
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
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
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
        usuario.setPassword("hash-actual");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("contraseñaActual", "hash-actual")).thenReturn(true);
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(id.toString());
        when(usuarioMapper.toDto(usuario)).thenReturn(dto);
        when(accessLogRepository.findByUsuarioIdOrderByTimestampDesc(id.toString())).thenReturn(List.of());
        UserExportDTO exportado = UserExportDTO.builder().id(id.toString()).build();
        when(usuarioExportConverter.toExportDto(eq(dto), eq(usuario), any())).thenReturn(exportado);

        assertEquals(exportado, service.exportUsuario("contraseñaActual"));
    }

    @Test
    void exportUsuario_sinContrasena_lanzaReautenticacionRequerida() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setPassword("hash-actual");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertThrows(ReautenticacionRequeridaException.class, () -> service.exportUsuario(""));
        verify(usuarioExportConverter, never()).toExportDto(any(), any(), any());
    }

    // ---- listarMisSolicitudes ----

    @Test
    void listarMisSolicitudes_devuelveLasSolicitudesDelPaciente() {
        autenticarComo("12345678A");
        when(solicitudAsignacionRepository.findByPacienteNifHashOrderByFechaCreacionDesc("hash-12345678A"))
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
        when(medicoPacienteRepository.findByMedicoIdAndPacienteId(solicitud.getMedico().getId(), solicitud.getPaciente().getId()))
                .thenReturn(List.of());

        SolicitudAsignacion resultado = service.actualizarEstadoSolicitud(solicitud.getId().toString(), "ACEPTADA");

        assertEquals("ACEPTADA", resultado.getEstado());
        verify(medicoPacienteRepository, times(1)).save(any());
        verify(notificacionRepository, times(1)).save(any());
        verify(auditoriaCambioService).registrarCambio(
                eq(solicitud.getPaciente().getId().toString()),
                eq(solicitud.getPaciente().getId().toString()),
                eq(solicitud.getMedico().getId().toString()),
                eq("ACEPTADA"),
                eq("solicitud_asignacion"),
                eq(solicitud.getId().toString()),
                eq("PENDIENTE"),
                eq("ACEPTADA"),
                eq(AuditoriaCambio.TipoOperacion.UPDATE),
                any());
    }

    @Test
    void actualizarEstadoSolicitud_aceptadaConRelacionActivaExistente_noCreaNiGuardaOtraRelacion() {
        autenticarComo("12345678A");
        SolicitudAsignacion solicitud = solicitudPendiente("12345678A");
        MedicoPaciente relacionActiva = new MedicoPaciente();
        relacionActiva.setEstado(MedicoPaciente.ESTADO_ACTIVA);
        when(solicitudAsignacionRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudAsignacionRepository.save(solicitud)).thenReturn(solicitud);
        when(medicoPacienteRepository.findByMedicoIdAndPacienteId(solicitud.getMedico().getId(), solicitud.getPaciente().getId()))
                .thenReturn(List.of(relacionActiva));

        service.actualizarEstadoSolicitud(solicitud.getId().toString(), "ACEPTADA");

        verify(medicoPacienteRepository, never()).save(any());
    }

    @Test
    void actualizarEstadoSolicitud_aceptadaConRelacionRevocada_reactivaEsaRelacion() {
        autenticarComo("12345678A");
        SolicitudAsignacion solicitud = solicitudPendiente("12345678A");
        MedicoPaciente relacionRevocada = new MedicoPaciente();
        relacionRevocada.setEstado(MedicoPaciente.ESTADO_REVOCADA);
        when(solicitudAsignacionRepository.findById(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudAsignacionRepository.save(solicitud)).thenReturn(solicitud);
        when(medicoPacienteRepository.findByMedicoIdAndPacienteId(solicitud.getMedico().getId(), solicitud.getPaciente().getId()))
                .thenReturn(List.of(relacionRevocada));

        service.actualizarEstadoSolicitud(solicitud.getId().toString(), "ACEPTADA");

        assertEquals(MedicoPaciente.ESTADO_ACTIVA, relacionRevocada.getEstado());
        verify(medicoPacienteRepository, times(1)).save(relacionRevocada);
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

    // ---- setupTotp / confirmTotp / disableTotp / isTotpEnabled ----

    @Test
    void setupTotp_generaUnSecretoPendienteYLoGuarda() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(totpService.generarSecreto()).thenReturn("SECRETO");
        when(totpService.generarOtpAuthUri("SECRETO", "12345678A")).thenReturn("otpauth://totp/HCC-TFM:12345678A?secret=SECRETO");

        TotpSetupResponseDTO resultado = service.setupTotp();

        assertEquals("SECRETO", resultado.getSecret());
        assertEquals("otpauth://totp/HCC-TFM:12345678A?secret=SECRETO", resultado.getOtpauthUri());
        assertEquals("SECRETO", usuario.getTotpSecret());
        assertEquals(false, usuario.isTotpEnabled());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void confirmTotp_conCodigoValido_activaElSegundoFactor() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setTotpSecret("SECRETO");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "123456")).thenReturn(true);

        service.confirmTotp("123456");

        assertEquals(true, usuario.isTotpEnabled());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void confirmTotp_sinSecretoPendiente_lanzaIllegalStateException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalStateException.class, () -> service.confirmTotp("123456"));
    }

    @Test
    void confirmTotp_conSegundoFactorYaActivo_lanzaIllegalStateException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setTotpSecret("SECRETO");
        usuario.setTotpEnabled(true);
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalStateException.class, () -> service.confirmTotp("123456"));
    }

    @Test
    void confirmTotp_conCodigoInvalido_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setTotpSecret("SECRETO");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "000000")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.confirmTotp("000000"));
    }

    @Test
    void disableTotp_conCodigoValido_desactivaYBorraElSecreto() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setTotpSecret("SECRETO");
        usuario.setTotpEnabled(true);
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "123456")).thenReturn(true);

        service.disableTotp("123456");

        assertEquals(false, usuario.isTotpEnabled());
        assertNull(usuario.getTotpSecret());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void disableTotp_conSegundoFactorNoActivo_lanzaIllegalStateException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalStateException.class, () -> service.disableTotp("123456"));
    }

    @Test
    void disableTotp_conCodigoInvalido_lanzaIllegalArgumentException() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setTotpSecret("SECRETO");
        usuario.setTotpEnabled(true);
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(totpService.validarCodigo("SECRETO", "000000")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.disableTotp("000000"));
    }

    @Test
    void isTotpEnabled_devuelveElEstadoDelUsuarioAutenticado() {
        autenticarComo("12345678A");
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        usuario.setTotpEnabled(true);
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));

        assertEquals(true, service.isTotpEnabled());
    }
}
