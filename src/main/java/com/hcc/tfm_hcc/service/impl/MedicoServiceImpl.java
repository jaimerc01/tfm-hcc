package com.hcc.tfm_hcc.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.NotificacionRepository;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.SolicitudAsignacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.service.UsuarioService;
import com.hcc.tfm_hcc.util.FechaUtils;
import com.hcc.tfm_hcc.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de médicos.
 * Proporciona funcionalidades para gestión de médicos, solicitudes y relaciones médico-paciente.
 * 
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicoServiceImpl implements MedicoService {

    // Constantes
    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_REVOCADA = "REVOCADA";
    private static final String PERFIL_MEDICO = "MEDICO";
    private static final String PERFIL_PACIENTE = "PACIENTE";
    
    // Constantes de campos
    private static final String CAMPO_DNI = "DNI";
    private static final String CAMPO_NIF = "NIF";
    private static final String CAMPO_DATOS_MEDICO = "Datos del médico";
    private static final String CAMPO_FECHA_NACIMIENTO = "Fecha de nacimiento";

    // Constantes de logging
    private static final String LOG_BUSCANDO_PACIENTE = "Buscando paciente con DNI: {} y fecha nacimiento: {}";
    private static final String LOG_PACIENTE_NO_ENCONTRADO = "Paciente no encontrado con DNI: {}";
    private static final String LOG_FECHA_NO_COINCIDE = "Fecha de nacimiento no coincide para DNI: {} - Fecha esperada: {}";
    private static final String LOG_PACIENTE_ENCONTRADO = "Paciente encontrado: {} con DNI: {}";
    private static final String LOG_LISTANDO_MEDICOS = "Listando todos los médicos del sistema";
    private static final String LOG_MEDICOS_LISTADOS = "Se encontraron {} médicos en el sistema";
    private static final String LOG_CREANDO_MEDICO = "Creando nuevo médico con NIF: {}";
    private static final String LOG_MEDICO_CREADO = "Médico creado exitosamente: {} con NIF: {}";
    private static final String LOG_ACTUALIZANDO_MEDICO = "Actualizando médico con ID: {}";
    private static final String LOG_MEDICO_ACTUALIZADO = "Médico actualizado exitosamente: {} con ID: {}";
    
    // Dependencies injection by constructor
    private final SolicitudAsignacionRepository solicitudAsignacionRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final MedicoPacienteRepository medicoPacienteRepository;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioService usuarioService;
    // Internal helper that creates a solicitud given both nifMedico and nifPaciente
    private SolicitudAsignacion crearSolicitudAsignacionConMedico(String nifMedico, String nifPaciente) {
        Usuario medico = usuarioRepository.findByNif(nifMedico).orElse(null);
        Usuario paciente = usuarioRepository.findByNif(nifPaciente).orElse(null);
        if (medico == null || paciente == null) return null;
        // Check for existing pending solicitud
        boolean exists = solicitudAsignacionRepository.existsByMedicoNifAndPacienteNifAndEstado(nifMedico, nifPaciente, ESTADO_PENDIENTE);
        if (exists) {
            throw new SolicitudExistenteException("Ya existe una solicitud pendiente para este médico y paciente");
        }
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setMedico(medico);
        solicitud.setPaciente(paciente);
        solicitud.setEstado(ESTADO_PENDIENTE);
        solicitud.setFechaCreacion(java.time.LocalDateTime.now());
        var saved = solicitudAsignacionRepository.save(solicitud);

        // Crear notificación para el paciente
        try {
            Notificacion n = new Notificacion();
            n.setMensaje("Has recibido una solicitud de asignación del médico " + (medico.getNombre()!=null ? medico.getNombre() : medico.getNif()));
            n.setLeida(false);
            n.setUsuario(paciente);
            n.setFechaCreacion(java.time.LocalDateTime.now());
            notificacionRepository.save(n);
        } catch (Exception ignored) {}

        return saved;
    }

    @Override
    public SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente) {
        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return null;
    return crearSolicitudAsignacionConMedico(nifMedico, nifPaciente);
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesPendientes() {
        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return Collections.emptyList();
        return solicitudAsignacionRepository.findByMedicoNifAndEstado(nifMedico, ESTADO_PENDIENTE);
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesEnviadas() {
        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return Collections.emptyList();
        return solicitudAsignacionRepository.findByMedicoNifOrderByFechaCreacionDesc(nifMedico);
    }

    /**
     * Busca un paciente por DNI y fecha de nacimiento.
     *
     * @param dni el DNI del paciente
     * @param fechaNacimiento la fecha de nacimiento en formato ISO
     * @return el DTO del paciente encontrado o null si no existe
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    @Override
    public PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento) {
        log.debug(LOG_BUSCANDO_PACIENTE, dni, fechaNacimiento);
        
        validarParametrosBusquedaPaciente(dni, fechaNacimiento);
        
        Usuario usuario = usuarioRepository.findByNif(dni).orElse(null);
        if (usuario == null) {
            log.debug(LOG_PACIENTE_NO_ENCONTRADO, dni);
            return null;
        }
        
        if (!validarFechaNacimientoPaciente(usuario, fechaNacimiento)) {
            log.debug(LOG_FECHA_NO_COINCIDE, dni, fechaNacimiento);
            return null;
        }
        
        PacienteDTO resultado = convertirAPacienteDTO(usuario);
        log.info(LOG_PACIENTE_ENCONTRADO, usuario.getNombre(), dni);
        
        return resultado;
    }

    /**
     * Valida los parámetros de búsqueda de paciente.
     *
     * @param dni el DNI del paciente
     * @param fechaNacimiento la fecha de nacimiento
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    private void validarParametrosBusquedaPaciente(String dni, String fechaNacimiento) {
        if (dni == null || dni.trim().isEmpty()) {
            String error = ErrorMessages.campoRequerido(CAMPO_DNI);
            log.error(error);
            throw new IllegalArgumentException(error);
        }
        if (fechaNacimiento == null || fechaNacimiento.trim().isEmpty()) {
            String error = ErrorMessages.campoRequerido(CAMPO_FECHA_NACIMIENTO);
            log.error(error);
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * Valida que la fecha de nacimiento del usuario coincida con la proporcionada.
     *
     * @param usuario el usuario a validar
     * @param fechaNacimiento la fecha de nacimiento esperada
     * @return true si las fechas coinciden, false en caso contrario
     */
    private boolean validarFechaNacimientoPaciente(Usuario usuario, String fechaNacimiento) {
        if (usuario.getFechaNacimiento() == null) {
            return false;
        }
        String fechaUsuario = FechaUtils.toIsoDate(usuario.getFechaNacimiento());
        return fechaUsuario.equals(fechaNacimiento);
    }

    /**
     * Convierte un Usuario a PacienteDTO.
     *
     * @param usuario el usuario a convertir
     * @return el DTO del paciente
     */
    private PacienteDTO convertirAPacienteDTO(Usuario usuario) {
        PacienteDTO dto = new PacienteDTO();
        dto.setNombre(usuario.getNombre());
        dto.setApellido1(usuario.getApellido1());
        dto.setApellido2(usuario.getApellido2());
        dto.setNif(usuario.getNif());
        dto.setFechaNacimiento(FechaUtils.toIsoDate(usuario.getFechaNacimiento()));
        return dto;
    }

    /**
     * Lista todos los médicos del sistema.
     *
     * @return lista de DTOs de usuarios médicos
     * @throws RuntimeException si hay error en la consulta
     */
    @Override
    public List<UsuarioDTO> listarMedicos() {
        log.debug(LOG_LISTANDO_MEDICOS);
        try {
            List<UsuarioDTO> medicos = obtenerUsuariosMedicos();
            log.info(LOG_MEDICOS_LISTADOS, medicos.size());
            return medicos;
        } catch (Exception e) {
            String error = ErrorMessages.formatError(ErrorMessages.ERROR_LISTADO_MEDICOS, e.getMessage());
            log.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Obtiene la lista de usuarios que tienen perfil de médico.
     *
     * @return lista de DTOs de médicos
     */
    private List<UsuarioDTO> obtenerUsuariosMedicos() {
        return StreamSupport.stream(usuarioRepository.findAll().spliterator(), false)
            .filter(this::esMedico)
            .map(usuarioMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Verifica si un usuario tiene perfil de médico.
     *
     * @param usuario el usuario a verificar
     * @return true si es médico, false en caso contrario
     */
    private boolean esMedico(Usuario usuario) {
        return perfilUsuarioRepository.getPerfilesByNif(usuario.getNif())
            .stream()
            .anyMatch(p -> PERFIL_MEDICO.equalsIgnoreCase(p.getRol()));
    }

    /**
     * Crea un nuevo médico en el sistema.
     *
     * @param medicoDTO los datos del médico a crear
     * @return el DTO del médico creado
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si no se puede crear el médico
     */
    @Override
    public UsuarioDTO crearMedico(UsuarioDTO medicoDTO) {
        log.debug(LOG_CREANDO_MEDICO, medicoDTO.getNif());
        
        validarDatosMedico(medicoDTO);
        Perfil perfilMedico = obtenerPerfilMedico();
        
        try {
            medicoDTO.setEstadoCuenta(ESTADO_ACTIVO);
            Usuario usuario = usuarioService.altaUsuario(medicoDTO);
            asignarPerfilMedico(usuario, perfilMedico);
            
            UsuarioDTO resultado = usuarioMapper.toDto(usuario);
            log.info(LOG_MEDICO_CREADO, usuario.getNombre(), usuario.getNif());
            
            return resultado;
        } catch (Exception e) {
            String error = ErrorMessages.formatError(ErrorMessages.ERROR_CREAR_MEDICO, e.getMessage());
            log.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Valida los datos del médico antes de la creación.
     *
     * @param medicoDTO los datos del médico a validar
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validarDatosMedico(UsuarioDTO medicoDTO) {
        if (medicoDTO == null) {
            String error = ErrorMessages.campoRequerido(CAMPO_DATOS_MEDICO);
            log.error(error);
            throw new IllegalArgumentException(error);
        }
        if (medicoDTO.getNif() == null || medicoDTO.getNif().trim().isEmpty()) {
            String error = ErrorMessages.campoRequerido(CAMPO_NIF);
            log.error(error);
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * Obtiene el perfil de médico del sistema.
     *
     * @return el perfil de médico
     * @throws IllegalStateException si no se encuentra el perfil
     */
    private Perfil obtenerPerfilMedico() {
        return perfilRepository.getPerfilByRol(PERFIL_MEDICO)
            .orElseThrow(() -> {
                String error = ErrorMessages.formatError(ErrorMessages.PERFIL_NO_ENCONTRADO, PERFIL_MEDICO);
                log.error(error);
                return new IllegalStateException(error);
            });
    }

    /**
     * Asigna el perfil de médico al usuario.
     *
     * @param usuario el usuario al que asignar el perfil
     * @param perfilMedico el perfil de médico
     */
    private void asignarPerfilMedico(Usuario usuario, Perfil perfilMedico) {
        PerfilUsuario perfilUsuario = new PerfilUsuario();
        perfilUsuario.setPerfil(perfilMedico);
        perfilUsuario.setUsuario(usuario);
        perfilUsuarioRepository.save(perfilUsuario);
    }

    /**
     * Actualiza los datos de un médico existente.
     *
     * @param id el ID del médico
     * @param medicoDTO los nuevos datos del médico
     * @return el DTO del médico actualizado
     * @throws IllegalArgumentException si el médico no existe
     */
    @Override
    public UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO) {
        log.debug(LOG_ACTUALIZANDO_MEDICO, id);
        
        Usuario usuario = buscarMedicoPorId(id);
        actualizarDatosUsuario(usuario, medicoDTO);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        
        UsuarioDTO resultado = usuarioMapper.toDto(usuarioActualizado);
        log.info(LOG_MEDICO_ACTUALIZADO, usuario.getNombre(), id);
        
        return resultado;
    }

    /**
     * Busca un médico por su ID.
     *
     * @param id el ID del médico
     * @return el usuario médico
     * @throws IllegalArgumentException si no se encuentra el médico
     */
    private Usuario buscarMedicoPorId(UUID id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> {
                String error = ErrorMessages.formatError(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO, id.toString());
                log.error(error);
                return new IllegalArgumentException(error);
            });
    }

    /**
     * Actualiza los datos del usuario con los valores proporcionados.
     *
     * @param usuario el usuario a actualizar
     * @param medicoDTO los nuevos datos
     */
    private void actualizarDatosUsuario(Usuario usuario, UsuarioDTO medicoDTO) {
        if (medicoDTO.getNombre() != null) {
            usuario.setNombre(medicoDTO.getNombre());
        }
        if (medicoDTO.getApellido1() != null) {
            usuario.setApellido1(medicoDTO.getApellido1());
        }
        if (medicoDTO.getApellido2() != null) {
            usuario.setApellido2(medicoDTO.getApellido2());
        }
        if (medicoDTO.getEmail() != null) {
            usuario.setEmail(medicoDTO.getEmail());
        }
        if (medicoDTO.getTelefono() != null) {
            usuario.setTelefono(medicoDTO.getTelefono());
        }
        if (medicoDTO.getEspecialidad() != null) {
            usuario.setEspecialidad(medicoDTO.getEspecialidad());
        }
    }

    @Override
    public void eliminarMedico(UUID id) {
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) throw new IllegalArgumentException("No existe el médico");
        Usuario usuario = usuarioOpt.get();
        // Notificar a pacientes asociados que el médico ha sido eliminado
        var relacionesMP = medicoPacienteRepository.findByMedicoIdAndEstadoNot(id, ESTADO_REVOCADA);
        if (relacionesMP != null && !relacionesMP.isEmpty()) {
            for (var rel : relacionesMP) {
                try {
                    Notificacion n = new Notificacion();
                    n.setMensaje("Tu médico " + (usuario.getNombre()!=null?usuario.getNombre():usuario.getNif()) + " ya no está disponible");
                    n.setLeida(false);
                    n.setUsuario(rel.getPaciente());
                    n.setFechaCreacion(java.time.LocalDateTime.now());
                    notificacionRepository.save(n);
                } catch (Exception ignored) {}
            }
        }
        var relaciones = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
            .filter(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase(PERFIL_MEDICO))
            .collect(Collectors.toList());
        perfilUsuarioRepository.deleteAll(relaciones);
        usuarioRepository.delete(usuario);
    }

    @Override
    public void setPerfilMedico(UUID id, boolean asignar) {
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) throw new IllegalArgumentException("Usuario no encontrado");
        Usuario usuario = usuarioOpt.get();
        var perfilMedicoOpt = perfilRepository.getPerfilByRol(PERFIL_MEDICO);
        var perfilPacienteOpt = perfilRepository.getPerfilByRol(PERFIL_PACIENTE);
        if (perfilMedicoOpt.isEmpty() || perfilPacienteOpt.isEmpty()) throw new IllegalStateException("Perfiles requeridos no configurados");

        var perfilMedico = perfilMedicoOpt.get();
        var perfilPaciente = perfilPacienteOpt.get();

        if (asignar) {
            // añadir relación PerfilUsuario MEDICO si no existe
            boolean already = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
                .anyMatch(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase(PERFIL_MEDICO));
            if (!already) {
                PerfilUsuario pu = new PerfilUsuario();
                pu.setPerfil(perfilMedico);
                pu.setUsuario(usuario);
                pu.setFechaCreacion(java.time.LocalDateTime.now());
                perfilUsuarioRepository.save(pu);
            }
        } else {
            // Si tiene relaciones MedicoPaciente, marcarlas como REVOCADA
            var relacionesMP = medicoPacienteRepository.findByMedicoIdAndEstadoNot(id, ESTADO_REVOCADA);
            if (relacionesMP != null && !relacionesMP.isEmpty()) {
                for (var rel : relacionesMP) {
                    rel.setEstado(ESTADO_REVOCADA);
                    rel.setFechaUltimaModificacion(java.time.LocalDateTime.now());
                    try {
                        Notificacion n = new Notificacion();
                        n.setMensaje("Tu médico " + (usuario.getNombre()!=null?usuario.getNombre():usuario.getNif()) + " ya no está asociado a tu cuenta");
                        n.setLeida(false);
                        n.setUsuario(rel.getPaciente());
                        n.setFechaCreacion(java.time.LocalDateTime.now());
                        notificacionRepository.save(n);
                    } catch (Exception ignored) {}
                }
                medicoPacienteRepository.saveAll(relacionesMP);
            }
            // revocar perfil MEDICO
            var relaciones = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
                .filter(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase(PERFIL_MEDICO))
                .collect(Collectors.toList());
            if (!relaciones.isEmpty()) {
                perfilUsuarioRepository.deleteAll(relaciones);
            }
            // garantizar que tenga perfil PACIENTE
            boolean hasPaciente = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
                .anyMatch(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase(PERFIL_PACIENTE));
            if (!hasPaciente) {
                PerfilUsuario pu2 = new PerfilUsuario();
                pu2.setPerfil(perfilPaciente);
                pu2.setUsuario(usuario);
                pu2.setFechaCreacion(java.time.LocalDateTime.now());
                perfilUsuarioRepository.save(pu2);
            }
        }
    }
}
