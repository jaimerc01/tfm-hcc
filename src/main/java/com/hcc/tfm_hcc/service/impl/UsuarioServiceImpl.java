package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.ReautenticacionRequeridaException;
import com.hcc.tfm_hcc.dto.TotpSetupResponseDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.converter.UsuarioExportConverter;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.NotificacionRepository;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.repository.SolicitudAsignacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.PerfilUsuarioService;
import com.hcc.tfm_hcc.service.TotpService;
import com.hcc.tfm_hcc.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    // Constantes
    private static final Log log = LogFactory.getLog(UsuarioServiceImpl.class);
    private static final String PERFIL_PACIENTE = "PACIENTE";
    private static final String SOLICITUD_ASIGNACION_TABLA = "solicitud_asignacion";
    private static final String TELEFONO_REGEX = "^[0-9+\\-() ]{0,20}$";
    private static final String NIF_REGEX = "^[0-9A-Za-z]{6,15}$";
    private static final int MAX_LONGITUD_NOMBRE = 100;
    private static final String ZONE_ID_EUROPE_MADRID = "Europe/Madrid"; // Zona horaria para la creación de usuarios
    
    // Dependencies injection by constructor
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilUsuarioService perfilUsuarioService;
    private final AccessLogRepository accessLogRepository;
    private final SolicitudAsignacionRepository solicitudAsignacionRepository;
    private final NotificacionRepository notificacionRepository;
    private final MedicoPacienteRepository medicoPacienteRepository;
    private final UsuarioExportConverter usuarioExportConverter;
    private final HmacSearchIndexService hmacSearchIndexService;
    private final TotpService totpService;
    private final AuditoriaCambioService auditoriaCambioService;

    /**
     * Obtiene el NIF del usuario autenticado actual
     */
    private String getNifUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return auth.getName();
    }

    /**
     * Obtiene el usuario autenticado actual
     */
    private Usuario obtenerUsuarioAutenticado() {
        String nif = getNifUsuarioAutenticado();
        if (nif == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        return findUsuarioByNif(nif)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Busca un usuario por NIF a través de su índice de búsqueda (HMAC), ya que el NIF
     * en sí está cifrado de forma no determinista y no es consultable por igualdad.
     */
    private Optional<Usuario> findUsuarioByNif(String nif) {
        if (nif == null || nif.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nif));
    }

    /**
     * Busca un usuario por email a través de su índice de búsqueda (HMAC), ya que el email
     * en sí está cifrado de forma no determinista y no es consultable por igualdad.
     */
    private Optional<Usuario> findUsuarioByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailHash(hmacSearchIndexService.indexar(email));
    }

    /**
     * Comprueba si existe un usuario con el mismo NIF.
     */
    private boolean existsUsuarioWithNif(String nif, UUID excludeId) {
        if (nif == null || nif.isBlank()) {
            return false;
        }

        String nifHash = hmacSearchIndexService.indexar(nif);
        if (excludeId == null) {
            return usuarioRepository.findByNifHash(nifHash).isPresent();
        }

        return usuarioRepository.existsByNifHashAndIdNot(nifHash, excludeId);
    }

    /**
     * Comprueba si existe un usuario con el mismo email.
     */
    private boolean existsUsuarioWithEmail(String email, UUID excludeId) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String emailHash = hmacSearchIndexService.indexar(email);
        if (excludeId == null) {
            return usuarioRepository.findByEmailHash(emailHash).isPresent();
        }

        return usuarioRepository.existsByEmailHashAndIdNot(emailHash, excludeId);
    }

    /**
     * Valida los datos del usuario antes del alta
     */
    private void validarUsuarioDTO(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }
        
        if (usuarioDTO.getNif() == null || usuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (usuarioDTO.getPassword() == null || usuarioDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("password"));
        }
        
        if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("email"));
        }
        
        validarFormatoEmail(usuarioDTO.getEmail());
    }

    /**
     * Valida el formato del email
     */
    private void validarFormatoEmail(String email) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_EMAIL_INVALIDO);
        }
    }

    /**
     * Valida el formato del teléfono
     */
    private void validarFormatoTelefono(String telefono) {
        if (telefono != null && !telefono.isBlank() && !telefono.matches(TELEFONO_REGEX)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_TELEFONO_INVALIDO);
        }
    }

    /**
     * Valida el formato del NIF
     */
    private void validarFormatoNif(String nif) {
        if (!nif.matches(NIF_REGEX)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_DNI_INVALIDO);
        }
    }

    /**
     * Valida la longitud de un campo
     */
    private void validarLongitudCampo(String valor, String nombreCampo, int longitudMaxima) {
        if (valor != null && valor.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                ErrorMessages.longitudInvalida(nombreCampo, valor.length(), longitudMaxima)
            );
        }
    }

    @Override
    @Transactional
    public Usuario altaUsuario(UsuarioDTO usuarioDTO) {
        validarUsuarioDTO(usuarioDTO);

        if (findUsuarioByNif(usuarioDTO.getNif()).isPresent()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_DNI_YA_EXISTE);
        }

        if (findUsuarioByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_EMAIL_YA_EXISTE);
        }

        // Codificar password
        usuarioDTO.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));

        // Crear usuario. Se sobrescriben explícitamente todos los campos que no debe
        // poder fijar quien da de alta al usuario (id incluido: un id no nulo hace que
        // JPA trate el guardado como una actualización de la fila existente con ese id
        // en vez de una inserción, así que dejarlo pasar permitiría sobrescribir una
        // cuenta ajena si el llamador lo hubiera fijado).
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setId(null);
        usuario.setNifHash(hmacSearchIndexService.indexar(usuario.getNif()));
        usuario.setEmailHash(hmacSearchIndexService.indexar(usuario.getEmail()));
        usuario.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_MADRID)));
        usuario.setLastPasswordChange(null);
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ACTIVO); // Estado activo por defecto al registrarse
        
        usuario = usuarioRepository.save(usuario);
        
        // Crear perfil PACIENTE obligatorio
        perfilUsuarioService.asignarPerfil(usuario.getId(), PERFIL_PACIENTE);
        
        return usuario;
    }

    @Override
    public String getNombreUsuario() {
        String nif = getNifUsuarioAutenticado();
        return findUsuarioByNif(nif)
                .map(usuario -> {
                    String nombreCompleto = usuario.getNombre();
                    if (usuario.getApellido1() != null && !usuario.getApellido1().isEmpty()) {
                        nombreCompleto += " " + usuario.getApellido1();
                    }
                    if (usuario.getApellido2() != null && !usuario.getApellido2().isEmpty()) {
                        nombreCompleto += " " + usuario.getApellido2();
                    }
                    return nombreCompleto;
                })
                .orElse(null);
    }

    @Override
    public UsuarioDTO getUsuarioActual() {
        String nif = getNifUsuarioAutenticado();
        if (nif == null) {
            return null;
        }
        return findUsuarioByNif(nif)
                .map(usuarioMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        Usuario usuario = obtenerUsuarioAutenticado();
        
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_CREDENCIALES_INVALIDAS);
        }
        
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setLastPasswordChange(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_MADRID)));
        usuarioRepository.save(usuario);
    }

    /**
     * Valida los datos parciales para actualización
     */
    private void validarDatosActualizacion(UsuarioDTO parcial, Usuario usuarioExistente) {
        if (parcial.getNombre() != null) {
            validarLongitudCampo(parcial.getNombre(), "nombre", MAX_LONGITUD_NOMBRE);
        }
        
        if (parcial.getApellido1() != null) {
            validarLongitudCampo(parcial.getApellido1(), "primer apellido", MAX_LONGITUD_NOMBRE);
        }
        
        if (parcial.getApellido2() != null) {
            validarLongitudCampo(parcial.getApellido2(), "segundo apellido", MAX_LONGITUD_NOMBRE);
        }
        
        if (parcial.getEmail() != null) {
            validarFormatoEmail(parcial.getEmail());
            validarEmailUnico(parcial.getEmail(), usuarioExistente.getId());
        }
        
        if (parcial.getTelefono() != null && !parcial.getTelefono().isBlank()) {
            validarFormatoTelefono(parcial.getTelefono());
        }
        
        if (parcial.getNif() != null) {
            validarFormatoNif(parcial.getNif());
            if (!parcial.getNif().equals(usuarioExistente.getNif())) {
                validarNifUnico(parcial.getNif(), usuarioExistente.getId());
            }
        }
    }

    /**
     * Valida que el email no esté en uso por otro usuario
     */
    private void validarEmailUnico(String email, UUID usuarioId) {
        if (existsUsuarioWithEmail(email, usuarioId)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_EMAIL_YA_EXISTE);
        }
    }

    /**
     * Valida que el NIF no esté en uso por otro usuario
     */
    private void validarNifUnico(String nif, UUID usuarioId) {
        if (existsUsuarioWithNif(nif, usuarioId)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_DNI_YA_EXISTE);
        }
    }

    /**
     * Actualiza los campos del usuario con los valores proporcionados
     */
    @NonNull
    private Usuario actualizarCamposUsuario(Usuario usuario, UsuarioDTO parcial) {
        if (parcial.getNombre() != null) {
            usuario.setNombre(parcial.getNombre());
        }
        if (parcial.getApellido1() != null) {
            usuario.setApellido1(parcial.getApellido1());
        }
        if (parcial.getApellido2() != null) {
            usuario.setApellido2(parcial.getApellido2());
        }
        if (parcial.getEmail() != null) {
            usuario.setEmail(parcial.getEmail());
            usuario.setEmailHash(hmacSearchIndexService.indexar(parcial.getEmail()));
        }
        if (parcial.getTelefono() != null) {
            usuario.setTelefono(parcial.getTelefono());
        }
        if (parcial.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(parcial.getFechaNacimiento());
        }
        if (parcial.getNif() != null && !parcial.getNif().equals(usuario.getNif())) {
            usuario.setNif(parcial.getNif());
            usuario.setNifHash(hmacSearchIndexService.indexar(parcial.getNif()));
        }

        return usuario;
    }

    @Override
    @Transactional
    public UsuarioDTO updateUsuarioActual(UsuarioDTO parcial) {
        Usuario usuario = obtenerUsuarioAutenticado();
        
        validarDatosActualizacion(parcial, usuario);
        Usuario usuarioActualizado = actualizarCamposUsuario(usuario, parcial);
        
        usuarioRepository.save(usuarioActualizado);
        return usuarioMapper.toDto(usuarioActualizado);
    }

    /**
     * Realiza la anonimización de datos del usuario
     */
    @NonNull
    private Usuario anonimizarUsuario(Usuario usuario) {
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ELIMINADO);
        usuario.setFechaEliminacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_MADRID)));
        usuario.setNombre("_eliminado_");
        usuario.setApellido1(null);
        usuario.setApellido2(null);
        String emailAnonimizado = "anon-" + usuario.getId() + "@local";
        usuario.setEmail(emailAnonimizado);
        usuario.setEmailHash(hmacSearchIndexService.indexar(emailAnonimizado));
        usuario.setTelefono(null);
        String nifAnonimizado = "DEL-" + usuario.getId().toString().substring(0, 8);
        usuario.setNif(nifAnonimizado);
        usuario.setNifHash(hmacSearchIndexService.indexar(nifAnonimizado));
        usuario.setEspecialidad(null);
        // La fecha de nacimiento es un cuasi-identificador (combinada con otros datos del
        // historial puede reidentificar a la persona) y debe anonimizarse igual que el
        // resto de datos personales de esta cuenta; se dejaba fuera hasta ahora.
        usuario.setFechaNacimiento(null);
        return usuario;
    }

    @Override
    @Transactional
    public void deleteCuentaActual(String currentPassword) {
        Usuario usuario = obtenerUsuarioAutenticado();
        verificarPasswordActual(usuario, currentPassword);
        Usuario usuarioAnonimizado = anonimizarUsuario(usuario);
        usuarioRepository.save(usuarioAnonimizado);
    }

    /**
     * Exige confirmar la contraseña actual antes de una operación sensible (exportar
     * todos los datos del usuario, eliminar la cuenta).
     */
    private void verificarPasswordActual(Usuario usuario, String currentPassword) {
        if (currentPassword == null || currentPassword.isBlank()
                || !passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new ReautenticacionRequeridaException(ErrorMessages.ERROR_REAUTENTICACION_REQUERIDA);
        }
    }

    @Override
    @Transactional
    public void limitarTratamiento() {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (Usuario.ESTADO_CUENTA_ELIMINADO.equals(usuario.getEstadoCuenta())) {
            throw new IllegalStateException(ErrorMessages.ERROR_CUENTA_ELIMINADA_NO_MODIFICABLE);
        }
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_SUSPENDIDO);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void reanudarTratamiento() {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (Usuario.ESTADO_CUENTA_ELIMINADO.equals(usuario.getEstadoCuenta())) {
            throw new IllegalStateException(ErrorMessages.ERROR_CUENTA_ELIMINADA_NO_MODIFICABLE);
        }
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ACTIVO);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserExportDTO.AccesoDTO> getMisLogs(LocalDateTime desde, LocalDateTime hasta) {
        Usuario usuario = obtenerUsuarioAutenticado();
        UsuarioDTO dto = usuarioMapper.toDto(usuario);
        
        var logs = (desde != null && hasta != null)
                ? accessLogRepository.findByUsuarioIdAndTimestampBetweenOrderByTimestampDesc(dto.getId(), desde, hasta)
                : accessLogRepository.findByUsuarioIdOrderByTimestampDesc(dto.getId());
                
        return logs.stream()
            .map(usuarioExportConverter::toAccesoDto)
                .toList();
    }

    /**
     * Construye el objeto UserExportDTO con los datos del usuario y sus accesos
     */
    @Override
    @Transactional(readOnly = true)
    public UserExportDTO exportUsuario(String currentPassword) {
        Usuario usuario = obtenerUsuarioAutenticado();
        verificarPasswordActual(usuario, currentPassword);
        UsuarioDTO dto = usuarioMapper.toDto(usuario);
        var logs = accessLogRepository.findByUsuarioIdOrderByTimestampDesc(dto.getId());

        return usuarioExportConverter.toExportDto(dto, usuario, logs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudAsignacion> listarMisSolicitudes() {
        String nif = getNifUsuarioAutenticado();
        if (nif == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        
        return solicitudAsignacionRepository.findByPacienteNifHashOrderByFechaCreacionDesc(hmacSearchIndexService.indexar(nif));
    }

    /**
     * Valida el estado de la solicitud
     */
    private void validarEstadoSolicitud(String nuevoEstado) {
        if (!nuevoEstado.equalsIgnoreCase(SolicitudAsignacion.ESTADO_ACEPTADA) && !nuevoEstado.equalsIgnoreCase(SolicitudAsignacion.ESTADO_RECHAZADA)) {
            throw new IllegalArgumentException(ErrorMessages.formatError("Estado inválido: {0}", nuevoEstado));
        }
    }

    /**
     * Valida que el usuario puede modificar la solicitud
     */
    private void validarPermisoModificacion(SolicitudAsignacion solicitud, String nifUsuario) {
        if (!solicitud.getPaciente().getNif().equals(nifUsuario)) {
            throw new IllegalStateException(ErrorMessages.ERROR_ACCESO_DENEGADO);
        }
    }

    /**
     * Al aceptarse la solicitud, garantiza que exista una relación médico-paciente
     * activa: si ya la hay no hace nada; si existe pero fue revocada (el paciente o el
     * médico la habían finalizado antes) la reactiva en lugar de crear un duplicado; y
     * si no existe ninguna, la crea.
     */
    private void procesarSolicitudAceptada(SolicitudAsignacion solicitud) {
        if (!SolicitudAsignacion.ESTADO_ACEPTADA.equalsIgnoreCase(solicitud.getEstado())) {
            return;
        }
        var medico = solicitud.getMedico();
        var paciente = solicitud.getPaciente();
        if (medico == null || paciente == null) {
            return;
        }

        List<MedicoPaciente> relaciones = medicoPacienteRepository.findByMedicoIdAndPacienteId(
            medico.getId(), paciente.getId());

        boolean yaActiva = relaciones.stream()
            .anyMatch(relacion -> MedicoPaciente.ESTADO_ACTIVA.equals(relacion.getEstado()));
        if (yaActiva) {
            return;
        }

        Optional<MedicoPaciente> previa = relaciones.stream().findFirst();
        if (previa.isPresent()) {
            MedicoPaciente relacion = previa.get();
            relacion.setEstado(MedicoPaciente.ESTADO_ACTIVA);
            medicoPacienteRepository.save(relacion);
            return;
        }

        crearRelacionMedicoPaciente(medico, paciente);
    }

    /**
     * Crea una nueva relación médico-paciente
     */
    private void crearRelacionMedicoPaciente(Usuario medico, Usuario paciente) {
        MedicoPaciente relacion = new MedicoPaciente();
        relacion.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_MADRID)));
        relacion.setEstado(MedicoPaciente.ESTADO_ACTIVA);
        relacion.setMedico(medico);
        relacion.setPaciente(paciente);
        medicoPacienteRepository.save(relacion);
    }

    /**
     * Envía notificación al médico sobre la respuesta del paciente
     */
    // TODO: Internacionalizar el mensaje de notificación según el idioma del usuario
    private void enviarNotificacionMedico(SolicitudAsignacion solicitud) {
        try {
            var medico = solicitud.getMedico();
            var paciente = solicitud.getPaciente();
            
            if (medico != null) {
                String nombrePaciente = (paciente != null && paciente.getNombre() != null) 
                    ? paciente.getNombre() 
                    : "un paciente";
                    
                String accion = SolicitudAsignacion.ESTADO_ACEPTADA.equalsIgnoreCase(solicitud.getEstado()) 
                    ? "aceptado" 
                    : solicitud.getEstado().toLowerCase();
                    
                String mensaje = String.format("El paciente %s ha %s la solicitud.", nombrePaciente, accion);
                
                Notificacion notificacion = new Notificacion();
                notificacion.setMensaje(mensaje);
                notificacion.setLeida(false);
                notificacion.setUsuario(medico);
                notificacion.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_MADRID)));
                notificacionRepository.save(notificacion);
            }
        } catch (Exception e) {
            // Error en notificación no debe afectar la operación principal
            log.error("Error al enviar notificación al médico", e);
        }
    }

    @Override
    @Transactional
    public SolicitudAsignacion actualizarEstadoSolicitud(String idSolicitud, String nuevoEstado) {
        String nif = getNifUsuarioAutenticado();
        if (nif == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        validarEstadoSolicitud(nuevoEstado);
        
        if (idSolicitud == null || idSolicitud.isBlank()) {
            throw new IllegalArgumentException(ErrorMessages.formatError("ID de solicitud inválido: {0}", idSolicitud));
        }
        
        UUID idSolicitudUUID = UUID.fromString(idSolicitud);
        if (idSolicitudUUID == null) {
            throw new IllegalArgumentException(ErrorMessages.formatError("ID de solicitud inválido: {0}", idSolicitud));
        }
        
        SolicitudAsignacion solicitud = solicitudAsignacionRepository.findById(idSolicitudUUID)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.formatError("Solicitud no encontrada: {0}", idSolicitud)));
        
        validarPermisoModificacion(solicitud, nif);

        String estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado.toUpperCase());

        SolicitudAsignacion solicitudGuardada = solicitudAsignacionRepository.save(solicitud);

        procesarSolicitudAceptada(solicitudGuardada);
        enviarNotificacionMedico(solicitudGuardada);
        registrarAuditoriaResolucionSolicitud(solicitudGuardada, estadoAnterior);

        return solicitudGuardada;
    }

    /**
     * Registra en la auditoría de cambios la resolución (aceptación o rechazo) de una
     * solicitud de asignación médico-paciente por parte del paciente destinatario.
     */
    private void registrarAuditoriaResolucionSolicitud(SolicitudAsignacion solicitud, String estadoAnterior) {
        Usuario paciente = solicitud.getPaciente();
        Usuario medico = solicitud.getMedico();

        auditoriaCambioService.registrarCambio(
            paciente.getId().toString(),
            paciente.getId().toString(),
            medico != null ? medico.getId().toString() : null,
            solicitud.getEstado(),
            SOLICITUD_ASIGNACION_TABLA,
            solicitud.getId().toString(),
            estadoAnterior,
            solicitud.getEstado(),
            AuditoriaCambio.TipoOperacion.UPDATE,
            "Resolución de solicitud de asignación médico-paciente"
        );
    }

    // ---- Segundo factor (TOTP) ----

    @Override
    @Transactional
    public TotpSetupResponseDTO setupTotp() {
        Usuario usuario = obtenerUsuarioAutenticado();

        String secreto = totpService.generarSecreto();
        usuario.setTotpSecret(secreto);
        usuario.setTotpEnabled(false);
        usuarioRepository.save(usuario);

        String otpauthUri = totpService.generarOtpAuthUri(secreto, usuario.getNif());
        return new TotpSetupResponseDTO(secreto, otpauthUri);
    }

    @Override
    @Transactional
    public void confirmTotp(String code) {
        Usuario usuario = obtenerUsuarioAutenticado();

        if (usuario.getTotpSecret() == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_TOTP_NO_CONFIGURADO);
        }
        if (usuario.isTotpEnabled()) {
            throw new IllegalStateException(ErrorMessages.ERROR_TOTP_YA_ACTIVO);
        }
        if (!totpService.validarCodigo(usuario.getTotpSecret(), code)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_TOTP_CODIGO_INVALIDO);
        }

        usuario.setTotpEnabled(true);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void disableTotp(String code) {
        Usuario usuario = obtenerUsuarioAutenticado();

        if (!usuario.isTotpEnabled()) {
            throw new IllegalStateException(ErrorMessages.ERROR_TOTP_NO_ACTIVO);
        }
        if (!totpService.validarCodigo(usuario.getTotpSecret(), code)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_TOTP_CODIGO_INVALIDO);
        }

        usuario.setTotpEnabled(false);
        usuario.setTotpSecret(null);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTotpEnabled() {
        return obtenerUsuarioAutenticado().isTotpEnabled();
    }
}
