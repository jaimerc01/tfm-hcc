package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.converter.PacienteConverter;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.PerfilUsuarioService;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.util.FechaUtils;

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
    private static final String ESTADO_REVOCADA = "REVOCADA";
    private static final String PERFIL_MEDICO = "MEDICO";
    private static final String PERFIL_PACIENTE = "PACIENTE";
    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";
    
    // Constantes de campos
    private static final String CAMPO_DNI = "DNI";
    private static final String CAMPO_NIF = "NIF";
    private static final String CAMPO_DATOS_MEDICO = "Datos del médico";
    private static final String CAMPO_FECHA_NACIMIENTO = "Fecha de nacimiento";
    
    // Dependencies injection by constructor
    private final NotificacionFacade notificacionFacade;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final MedicoPacienteRepository medicoPacienteRepository;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioFacade usuarioFacade;
    private final PacienteConverter pacienteConverter;
    private final PerfilUsuarioService perfilUsuarioService;

    /**
     * Busca un paciente por DNI y fecha de nacimiento.
     *
     * @param dni el DNI del paciente
     * @param fechaNacimiento la fecha de nacimiento en formato ISO
     * @return el DTO del paciente encontrado o null si no existe
    * @throws MedicoValidationException si los parámetros son inválidos
     */
    @Override
    public PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento) {
        log.debug("Buscando paciente con DNI: {} y fecha nacimiento: {}", dni, fechaNacimiento);
        
        validarParametrosBusquedaPaciente(dni, fechaNacimiento);
        
        Usuario usuario = usuarioRepository.findByNif(dni).orElse(null);
        if (usuario == null) {
            log.debug("Paciente no encontrado con DNI: {}", dni);
            return null;
        }
        
        if (!validarFechaNacimientoPaciente(usuario, fechaNacimiento)) {
            log.debug("Fecha de nacimiento no coincide para DNI: {} - Fecha esperada: {}", dni, fechaNacimiento);
            return null;
        }
        
        PacienteDTO resultado = pacienteConverter.toDto(usuario);
        log.info("Paciente encontrado: {} con DNI: {}", usuario.getNombre(), dni);
        
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
            throw new MedicoValidationException(error);
        }
        if (fechaNacimiento == null || fechaNacimiento.trim().isEmpty()) {
            String error = ErrorMessages.campoRequerido(CAMPO_FECHA_NACIMIENTO);
            log.error(error);
            throw new MedicoValidationException(error);
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
     * Lista todos los médicos del sistema.
     *
     * @return lista de DTOs de usuarios médicos
    * @throws MedicoOperacionException si hay error en la consulta
     */
    @Override
    public List<UsuarioDTO> listarMedicos() {
        log.debug("Listando todos los médicos del sistema");
        try {
            List<UsuarioDTO> medicos = obtenerUsuariosMedicos();
            log.info("Se encontraron {} médicos en el sistema", medicos.size());
            return medicos;
        } catch (Exception e) {
            String error = ErrorMessages.formatError(ErrorMessages.ERROR_LISTADO_MEDICOS, e.getMessage());
            log.error(error, e);
            throw new MedicoOperacionException(error, e);
        }
    }

    /**
     * Obtiene la lista de usuarios que tienen perfil de médico.
     *
     * @return lista de DTOs de médicos
     */
    private List<UsuarioDTO> obtenerUsuariosMedicos() {
        return perfilUsuarioService.listarUsuariosPorRol(PERFIL_MEDICO)
            .stream()
            .map(usuarioMapper::toDto)
            .toList();
    }

    /**
     * Crea un nuevo médico en el sistema.
     *
     * @param medicoDTO los datos del médico a crear
     * @return el DTO del médico creado
     * @throws MedicoValidationException si los datos son inválidos
     * @throws PerfilNotFoundException si no se puede crear el médico por falta de perfil
     */
    @Override
    public UsuarioDTO crearMedico(UsuarioDTO medicoDTO) {
        log.debug("Creando nuevo médico con NIF: {}", medicoDTO.getNif());
        
        validarDatosMedico(medicoDTO);
        
        try {
            medicoDTO.setEstadoCuenta(ESTADO_ACTIVO);
            UsuarioDTO usuarioDTO = usuarioFacade.altaUsuario(medicoDTO);
            perfilUsuarioService.asignarPerfil(UUID.fromString(usuarioDTO.getId()), PERFIL_MEDICO);
            
            log.info("Médico creado exitosamente: {} con NIF: {}", usuarioDTO.getNombre(), usuarioDTO.getNif());
            
            return usuarioDTO;
        } catch (Exception e) {
            String error = ErrorMessages.formatError(ErrorMessages.ERROR_CREAR_MEDICO, e.getMessage());
            log.error(error, e);
            throw new MedicoOperacionException(error, e);
        }
    }

    /**
     * Valida los datos del médico antes de la creación.
     *
     * @param medicoDTO los datos del médico a validar
     * @throws MedicoValidationException si los datos son inválidos
     */
    private void validarDatosMedico(UsuarioDTO medicoDTO) {
        if (medicoDTO == null) {
            String error = ErrorMessages.campoRequerido(CAMPO_DATOS_MEDICO);
            log.error(error);
            throw new MedicoValidationException(error);
        }
        if (medicoDTO.getNif() == null || medicoDTO.getNif().trim().isEmpty()) {
            String error = ErrorMessages.campoRequerido(CAMPO_NIF);
            log.error(error);
            throw new MedicoValidationException(error);
        }
    }

    /**
     * Actualiza los datos de un médico existente.
     *
     * @param id el ID del médico
     * @param medicoDTO los nuevos datos del médico
     * @return el DTO del médico actualizado
    * @throws UsuarioNoEncontradoException si el médico no existe
     */
    @Override
    public UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO) {
        log.debug("Actualizando médico con ID: {}", id);
        
        Usuario usuario = buscarMedicoPorId(id);
        if (usuario == null) {
            throw new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO);
        }
        actualizarDatosUsuario(usuario, medicoDTO);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        
        UsuarioDTO resultado = usuarioMapper.toDto(usuarioActualizado);
        log.info("Médico actualizado exitosamente: {} con ID: {}", usuario.getNombre(), id);
        
        return resultado;
    }

    /**
     * Busca un médico por su ID.
     *
     * @param id el ID del médico
     * @return el usuario médico
    * @throws MedicoValidationException si no se encuentra el médico
     */
    private Usuario buscarMedicoPorId(UUID id) {
        if(id == null) {
            String error = ErrorMessages.campoRequerido("ID del médico");
            log.error(error);
            throw new MedicoValidationException(error);
        }
        return usuarioRepository.findById(id)
            .orElseThrow(() -> {
                String error = ErrorMessages.formatError(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO, id.toString());
                log.error(error);
                return new UsuarioNoEncontradoException(error);
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
    public UUID eliminarMedico(UUID id) {
        if(id == null) {
            String error = ErrorMessages.campoRequerido("ID del médico");
            log.error(error);
            throw new MedicoValidationException(error);
        }
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) throw new UsuarioNoEncontradoException("No existe el médico");
        Usuario usuario = usuarioOpt.get();
        // Notificar a pacientes asociados que el médico ha sido eliminado
        var relacionesMP = medicoPacienteRepository.findByMedicoIdAndEstadoNot(id, ESTADO_REVOCADA);
        if (relacionesMP != null && !relacionesMP.isEmpty()) {
            for (var rel : relacionesMP) {
                String mensaje = "Tu médico " + (usuario.getNombre()!=null?usuario.getNombre():usuario.getNif()) + " ya no está disponible";
                notificacionFacade.crearNotificacionParaUsuario(rel.getPaciente().getNif(), mensaje);
                
            }
        }
        perfilUsuarioService.revocarPerfil(id, PERFIL_MEDICO);
        usuario.setEstadoCuenta("ELIMINADO");
        usuario.setFechaUltimaModificacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
        usuarioRepository.save(usuario);
        return id;
    }

    @Override
    public UUID setPerfilMedico(UUID id, boolean asignar) {

        if (perfilRepository.getPerfilByRol(PERFIL_MEDICO).isEmpty() || perfilRepository.getPerfilByRol(PERFIL_PACIENTE).isEmpty()) {
            throw new PerfilNotFoundException("Perfiles requeridos no configurados");
        }
        Usuario usuario = getUsuarioFromId(id);

        if (asignar) {
            perfilUsuarioService.asignarPerfil(id, PERFIL_MEDICO);
        } else {
            revocarPerfilMedico(id, usuario);
        }
        return id;
    }

    private void revocarPerfilMedico(UUID id, Usuario usuario) {
        // Si tiene relaciones MedicoPaciente, marcarlas como REVOCADA
        var relacionesMP = medicoPacienteRepository.findByMedicoIdAndEstadoNot(id, ESTADO_REVOCADA);
        if (relacionesMP != null && !relacionesMP.isEmpty()) {
            for (var rel : relacionesMP) {
                rel.setEstado(ESTADO_REVOCADA);
                rel.setFechaUltimaModificacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
                String mensaje = "Tu médico " + (usuario.getNombre()!=null?usuario.getNombre():usuario.getNif()) + " ya no está asociado a tu cuenta";
                notificacionFacade.crearNotificacionParaUsuario(rel.getPaciente().getNif(), mensaje);
            }
            medicoPacienteRepository.saveAll(relacionesMP);
        }
        // revocar perfil MEDICO
        perfilUsuarioService.revocarPerfil(id, PERFIL_MEDICO);
        // garantizar que tenga perfil PACIENTE
        if (!perfilUsuarioService.tienePerfil(id, PERFIL_PACIENTE)) {
            perfilUsuarioService.asignarPerfil(id, PERFIL_PACIENTE);
        }
    }

    private Usuario getUsuarioFromId(UUID id) {
        if (id == null) {
            String error = ErrorMessages.campoRequerido("ID del usuario");
            log.error(error);
            throw new MedicoValidationException(error);
        }
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado");
        }
        return usuarioOpt.get();
    }
}
