package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO.AccesoDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.NotificacionRepository;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.SolicitudAsignacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    // Constantes
    private static final String PERFIL_PACIENTE = "PACIENTE";
    private static final String EMAIL_REGEX = "^[^@\\n]+@[^@\\n]+\\.[^@\\n]+$";
    private static final String TELEFONO_REGEX = "^[0-9+\\-() ]{0,20}$";
    private static final String NIF_REGEX = "^[0-9A-Za-z]{6,15}$";
    private static final int MAX_LONGITUD_NOMBRE = 100;
    
    // Dependencies injection by constructor
    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilRepository perfilRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final AccessLogRepository accessLogRepository;
    private final SolicitudAsignacionRepository solicitudAsignacionRepository;
    private final NotificacionRepository notificacionRepository;
    private final MedicoPacienteRepository medicoPacienteRepository;

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
        return usuarioRepository.findByNif(nif)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
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
        if (!email.matches(EMAIL_REGEX)) {
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

    /**
     * Crea la relación PerfilUsuario con el perfil PACIENTE
     */
    private void crearPerfilPaciente(Usuario usuario) {
        var perfilOpt = perfilRepository.getPerfilByRol(PERFIL_PACIENTE);
        if (perfilOpt.isEmpty()) {
            throw new IllegalStateException(
                ErrorMessages.formatError("Perfil {0} no encontrado; se cancela el alta de usuario", PERFIL_PACIENTE)
            );
        }
        
        try {
            PerfilUsuario perfilUsuario = new PerfilUsuario();
            perfilUsuario.setPerfil(perfilOpt.get());
            perfilUsuario.setUsuario(usuario);
            perfilUsuario.setFechaCreacion(LocalDateTime.now());
            perfilUsuarioRepository.save(perfilUsuario);
        } catch (Exception e) {
            throw new RuntimeException(
                ErrorMessages.formatError("Error creando relación PerfilUsuario ({0})", PERFIL_PACIENTE), e
            );
        }
    }

    @Override
    @Transactional
    public Usuario altaUsuario(UsuarioDTO usuarioDTO) {
        validarUsuarioDTO(usuarioDTO);
        
        // Codificar password
        usuarioDTO.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        
        // Crear usuario
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setLastPasswordChange(LocalDateTime.now());
        
        usuario = usuarioRepository.save(usuario);
        
        // Crear perfil PACIENTE obligatorio
        crearPerfilPaciente(usuario);
        
        return usuario;
    }

    @Override
    public String getNombreUsuario() {
        String nif = getNifUsuarioAutenticado();
        return usuarioRepository.findByNif(nif)
                .map(Usuario::getNombre)
                .orElse(null);
    }

    @Override
    public UsuarioDTO getUsuarioActual() {
        String nif = getNifUsuarioAutenticado();
        if (nif == null) {
            return null;
        }
        return usuarioRepository.findByNif(nif)
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
        usuario.setFechaUltimaModificacion(LocalDateTime.now());
        usuario.setLastPasswordChange(LocalDateTime.now());
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
        if (usuarioRepository.existsByEmailAndIdNot(email, usuarioId)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_EMAIL_YA_EXISTE);
        }
    }

    /**
     * Valida que el NIF no esté en uso por otro usuario
     */
    private void validarNifUnico(String nif, UUID usuarioId) {
        if (usuarioRepository.existsByNifAndIdNot(nif, usuarioId)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_DNI_YA_EXISTE);
        }
    }

    /**
     * Actualiza los campos del usuario con los valores proporcionados
     */
    private void actualizarCamposUsuario(Usuario usuario, UsuarioDTO parcial) {
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
        }
        if (parcial.getTelefono() != null) {
            usuario.setTelefono(parcial.getTelefono());
        }
        if (parcial.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(parcial.getFechaNacimiento());
        }
        if (parcial.getNif() != null && !parcial.getNif().equals(usuario.getNif())) {
            usuario.setNif(parcial.getNif());
        }
        usuario.setFechaUltimaModificacion(LocalDateTime.now());
    }

    @Override
    @Transactional
    public UsuarioDTO updateUsuarioActual(UsuarioDTO parcial) {
        Usuario usuario = obtenerUsuarioAutenticado();
        
        validarDatosActualizacion(parcial, usuario);
        actualizarCamposUsuario(usuario, parcial);
        
        usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuario);
    }

    /**
     * Realiza la anonimización de datos del usuario
     */
    private void anonimizarUsuario(Usuario usuario) {
        usuario.setEstadoCuenta("ELIMINADO");
        usuario.setFechaEliminacion(LocalDateTime.now());
        usuario.setNombre("_eliminado_");
        usuario.setApellido1(null);
        usuario.setApellido2(null);
        usuario.setEmail("anon-" + usuario.getId() + "@local");
        usuario.setTelefono(null);
        usuario.setNif("DEL-" + usuario.getId().toString().substring(0, 8));
        usuario.setEspecialidad(null);
        usuario.setFechaUltimaModificacion(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteCuentaActual() {
        Usuario usuario = obtenerUsuarioAutenticado();
        anonimizarUsuario(usuario);
        usuarioRepository.save(usuario);
    }

    /**
     * Convierte un AccessLog a AccesoDTO
     */
    private AccesoDTO convertirAccessLogADTO(AccessLog log) {
        return AccesoDTO.builder()
                .timestamp(log.getTimestamp())
                .metodo(log.getMetodo())
                .ruta(log.getRuta())
                .estado(log.getEstado())
                .duracionMs(log.getDuracionMs())
                .ip(log.getIp())
                .userAgent(log.getUserAgent())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccesoDTO> getMisLogs(LocalDateTime desde, LocalDateTime hasta) {
        Usuario usuario = obtenerUsuarioAutenticado();
        UsuarioDTO dto = usuarioMapper.toDto(usuario);
        
        var logs = (desde != null && hasta != null)
                ? accessLogRepository.findByUsuarioIdAndTimestampBetweenOrderByTimestampDesc(dto.getId(), desde, hasta)
                : accessLogRepository.findByUsuarioIdOrderByTimestampDesc(dto.getId());
                
        return logs.stream()
                .map(this::convertirAccessLogADTO)
                .toList();
    }

    /**
     * Construye el objeto UserExportDTO con los datos del usuario y sus accesos
     */
    private UserExportDTO construirExportDTO(UsuarioDTO dto, List<AccessLog> logs) {
        var accesos = logs.stream()
                .limit(500)
                .map(this::convertirAccessLogParaExport)
                .toList();
                
        return UserExportDTO.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido1(dto.getApellido1())
                .apellido2(dto.getApellido2())
                .email(dto.getEmail())
                .nif(dto.getNif())
                .telefono(dto.getTelefono())
                .fechaNacimiento(dto.getFechaNacimiento())
                .fechaCreacion(dto.getFechaCreacion())
                .fechaUltimaModificacion(dto.getFechaUltimaModificacion())
                .estadoCuenta(usuarioRepository.findByNif(dto.getNif())
                        .map(Usuario::getEstadoCuenta)
                        .orElse(null))
                .fechaEliminacion(usuarioRepository.findByNif(dto.getNif())
                        .map(Usuario::getFechaEliminacion)
                        .orElse(null))
                .accesos(accesos)
                .build();
    }

    /**
     * Convierte AccessLog a UserExportDTO.AccesoDTO
     */
    private UserExportDTO.AccesoDTO convertirAccessLogParaExport(AccessLog log) {
        return UserExportDTO.AccesoDTO.builder()
                .timestamp(log.getTimestamp())
                .metodo(log.getMetodo())
                .ruta(log.getRuta())
                .estado(log.getEstado())
                .duracionMs(log.getDuracionMs())
                .ip(log.getIp())
                .userAgent(log.getUserAgent())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserExportDTO exportUsuario() {
        Usuario usuario = obtenerUsuarioAutenticado();
        UsuarioDTO dto = usuarioMapper.toDto(usuario);
        var logs = accessLogRepository.findByUsuarioIdOrderByTimestampDesc(dto.getId());
        
        return construirExportDTO(dto, logs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudAsignacion> listarMisSolicitudes() {
        String nif = getNifUsuarioAutenticado();
        if (nif == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        
        return solicitudAsignacionRepository.findByPacienteNifOrderByFechaCreacionDesc(nif);
    }

    /**
     * Valida el estado de la solicitud
     */
    private void validarEstadoSolicitud(String nuevoEstado) {
        if (!nuevoEstado.equalsIgnoreCase("ACEPTADA") && !nuevoEstado.equalsIgnoreCase("RECHAZADA")) {
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
     * Crea relación médico-paciente si la solicitud es aceptada
     */
    private void procesarSolicitudAceptada(SolicitudAsignacion solicitud) {
        if ("ACEPTADA".equalsIgnoreCase(solicitud.getEstado())) {
            var medico = solicitud.getMedico();
            var paciente = solicitud.getPaciente();
            
            if (medico != null && paciente != null) {
                boolean exists = medicoPacienteRepository.existsByMedicoIdAndPacienteId(
                    medico.getId(), paciente.getId()
                );
                
                if (!exists) {
                    crearRelacionMedicoPaciente(medico, paciente);
                }
            }
        }
    }

    /**
     * Crea una nueva relación médico-paciente
     */
    private void crearRelacionMedicoPaciente(Usuario medico, Usuario paciente) {
        MedicoPaciente relacion = new MedicoPaciente();
        relacion.setFechaCreacion(LocalDateTime.now());
        relacion.setEstado("ACTIVA");
        relacion.setMedico(medico);
        relacion.setPaciente(paciente);
        medicoPacienteRepository.save(relacion);
    }

    /**
     * Envía notificación al médico sobre la respuesta del paciente
     */
    private void enviarNotificacionMedico(SolicitudAsignacion solicitud) {
        try {
            var medico = solicitud.getMedico();
            var paciente = solicitud.getPaciente();
            
            if (medico != null) {
                String nombrePaciente = (paciente != null && paciente.getNombre() != null) 
                    ? paciente.getNombre() 
                    : "un paciente";
                    
                String accion = "ACEPTADA".equalsIgnoreCase(solicitud.getEstado()) 
                    ? "aceptado" 
                    : solicitud.getEstado().toLowerCase();
                    
                String mensaje = String.format("El paciente %s ha %s la solicitud.", nombrePaciente, accion);
                
                Notificacion notificacion = new Notificacion();
                notificacion.setMensaje(mensaje);
                notificacion.setLeida(false);
                notificacion.setUsuario(medico);
                notificacion.setFechaCreacion(LocalDateTime.now());
                notificacionRepository.save(notificacion);
            }
        } catch (Exception e) {
            // Error en notificación no debe afectar la operación principal
            // Se podría loggear el error aquí
        }
    }

    @Override
    @Transactional
    public SolicitudAsignacion actualizarEstadoSolicitud(String solicitudId, String nuevoEstado) {
        String nif = getNifUsuarioAutenticado();
        if (nif == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        validarEstadoSolicitud(nuevoEstado);
        
        UUID solicitudIdUUID = UUID.fromString(solicitudId);
        SolicitudAsignacion solicitud = solicitudAsignacionRepository.findById(solicitudIdUUID)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.formatError("Solicitud no encontrada: {0}", solicitudId)));
        
        validarPermisoModificacion(solicitud, nif);
        
        solicitud.setEstado(nuevoEstado.toUpperCase());
        solicitud.setFechaUltimaModificacion(LocalDateTime.now());
        
        SolicitudAsignacion solicitudGuardada = solicitudAsignacionRepository.save(solicitud);
        
        procesarSolicitudAceptada(solicitudGuardada);
        enviarNotificacionMedico(solicitudGuardada);
        
        return solicitudGuardada;
    }

}
