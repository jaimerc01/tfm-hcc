package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.converter.PacienteConverter;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.PerfilUsuarioService;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.service.PropuestaCambioClinicoService;
import com.hcc.tfm_hcc.util.FechaUtils;
import com.hcc.tfm_hcc.util.LogMaskUtil;

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
    private static final String PERFIL_MEDICO = "MEDICO";
    private static final String PERFIL_PACIENTE = "PACIENTE";
    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";

    // Textos de las notificaciones enviadas al paciente cuando pierde a su médico
    private static final String MENSAJE_MEDICO_PREFIJO = "Tu médico ";
    private static final String MENSAJE_MEDICO_NO_DISPONIBLE = " ya no está disponible";
    private static final String MENSAJE_MEDICO_DESASIGNADO = " ya no está asociado a tu cuenta";

    // Constantes de campos
    private static final String CAMPO_DNI = "DNI";
    private static final String CAMPO_NIF = "NIF";
    private static final String CAMPO_DATOS_MEDICO = "Datos del médico";
    private static final String CAMPO_FECHA_NACIMIENTO = "Fecha de nacimiento";
    private static final String CAMPO_ID_MEDICO = "ID del médico";
    private static final String CAMPO_ID_USUARIO = "ID del usuario";
    
    // Dependencies injection by constructor
    private final NotificacionFacade notificacionFacade;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final MedicoPacienteRepository medicoPacienteRepository;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioFacade usuarioFacade;
    private final PacienteConverter pacienteConverter;
    private final PerfilUsuarioService perfilUsuarioService;
    private final HmacSearchIndexService hmacSearchIndexService;
    private final PropuestaCambioClinicoService propuestaCambioClinicoService;

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
        String dniLog = LogMaskUtil.enmascarar(dni);
        log.debug("Buscando paciente con DNI: {} y fecha nacimiento: {}", dniLog, fechaNacimiento);

        validarParametrosBusquedaPaciente(dni, fechaNacimiento);

        Usuario usuario = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(dni)).orElse(null);
        if (usuario == null) {
            log.debug("Paciente no encontrado con DNI: {}", dniLog);
            return null;
        }

        if (!validarFechaNacimientoPaciente(usuario, fechaNacimiento)) {
            log.debug("Fecha de nacimiento no coincide para DNI: {} - Fecha esperada: {}", dniLog, fechaNacimiento);
            return null;
        }
        
        PacienteDTO resultado = pacienteConverter.toDto(usuario);
        log.info("Paciente encontrado: {} con DNI: {}", usuario.getNombre(), dniLog);
        
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
     * Lista los pacientes con una relación médico-paciente activa para un médico dado.
     *
     * @param nifMedico NIF del médico del cual listar los pacientes asignados
     * @return lista de DTOs de los pacientes actualmente asignados al médico
     * @throws UsuarioNoEncontradoException si no existe un usuario con ese NIF
     */
    @Override
    public List<PacienteDTO> listarMisPacientes(String nifMedico) {
        log.debug("Listando pacientes asignados al médico: {}", LogMaskUtil.enmascarar(nifMedico));

        Usuario medico = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nifMedico))
            .orElseThrow(() -> new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));

        List<PacienteDTO> pacientes = medicoPacienteRepository
            .findByMedicoIdAndEstado(medico.getId(), MedicoPaciente.ESTADO_ACTIVA)
            .stream()
            .map(MedicoPaciente::getPaciente)
            .map(pacienteConverter::toDto)
            .toList();

        log.info("Se encontraron {} pacientes asignados al médico", pacientes.size());
        return pacientes;
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
    @Transactional
    public UsuarioDTO crearMedico(UsuarioDTO medicoDTO) {
        validarDatosMedico(medicoDTO);
        log.debug("Creando nuevo médico con NIF: {}", LogMaskUtil.enmascarar(medicoDTO.getNif()));

        try {
            medicoDTO.setEstadoCuenta(Usuario.ESTADO_CUENTA_ACTIVO);
            UsuarioDTO usuarioDTO = usuarioFacade.altaUsuario(medicoDTO);
            perfilUsuarioService.asignarPerfil(UUID.fromString(usuarioDTO.getId()), PERFIL_MEDICO);

            log.info("Médico creado exitosamente: {} con NIF: {}", usuarioDTO.getNombre(), LogMaskUtil.enmascarar(usuarioDTO.getNif()));
            
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
    @Transactional
    public UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO) {
        log.debug("Actualizando médico con ID: {}", id);
        
        Usuario usuario = buscarMedicoPorId(id);
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
        if (id == null) {
            String error = ErrorMessages.campoRequerido(CAMPO_ID_MEDICO);
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
            usuario.setEmailHash(hmacSearchIndexService.indexar(medicoDTO.getEmail()));
        }
        if (medicoDTO.getTelefono() != null) {
            usuario.setTelefono(medicoDTO.getTelefono());
        }
        if (medicoDTO.getEspecialidad() != null) {
            usuario.setEspecialidad(medicoDTO.getEspecialidad());
        }
    }

    @Override
    @Transactional
    public UUID eliminarMedico(UUID id) {
        Usuario usuario = getUsuarioFromId(id);
        validarEsMedico(id);

        // Cerrar las relaciones médico-paciente antes de dar de baja la cuenta: si no,
        // quedarían en estado ACTIVA y el médico eliminado seguiría autorizado para
        // consultar el historial de esos pacientes (ver HistorialClinicoServiceImpl).
        revocarRelacionesMedicoPaciente(id, usuario, MENSAJE_MEDICO_NO_DISPONIBLE);

        perfilUsuarioService.revocarPerfil(id, PERFIL_MEDICO);
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ELIMINADO);
        usuario.setFechaEliminacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
        usuarioRepository.save(usuario);
        return id;
    }

    @Override
    @Transactional
    public UUID setPerfilMedico(UUID id, boolean asignar) {

        if (perfilRepository.getPerfilByRol(PERFIL_MEDICO).isEmpty() || perfilRepository.getPerfilByRol(PERFIL_PACIENTE).isEmpty()) {
            throw new PerfilNotFoundException(ErrorMessages.ERROR_CONFIGURACION);
        }
        Usuario usuario = getUsuarioFromId(id);

        if (asignar) {
            perfilUsuarioService.asignarPerfil(id, PERFIL_MEDICO);
        } else {
            validarEsMedico(id);
            revocarPerfilMedico(id, usuario);
        }
        return id;
    }

    /**
     * Comprueba que el usuario indicado tenga actualmente el perfil MEDICO.
     *
     * <p>Necesario porque {@link #eliminarMedico(UUID)} y la revocación de
     * {@link #setPerfilMedico(UUID, boolean)} actúan sobre cualquier ID de usuario
     * válido: sin esta comprobación, se podía dar de baja o alterar el rol de una
     * cuenta que nunca fue médico (por ejemplo, un administrador) simplemente
     * pasando su ID a estos endpoints.</p>
     *
     * @param id identificador del usuario a comprobar
     * @throws UsuarioNoEncontradoException si el usuario no tiene perfil MEDICO
     */
    private void validarEsMedico(UUID id) {
        if (!perfilUsuarioService.tienePerfil(id, PERFIL_MEDICO)) {
            log.warn("Operación de médico solicitada sobre un usuario sin perfil MEDICO: {}", id);
            throw new UsuarioNoEncontradoException(ErrorMessages.ERROR_MEDICO_NO_ENCONTRADO);
        }
    }

    private void revocarPerfilMedico(UUID id, Usuario usuario) {
        revocarRelacionesMedicoPaciente(id, usuario, MENSAJE_MEDICO_DESASIGNADO);
        // revocar perfil MEDICO
        perfilUsuarioService.revocarPerfil(id, PERFIL_MEDICO);
        // garantizar que tenga perfil PACIENTE
        if (!perfilUsuarioService.tienePerfil(id, PERFIL_PACIENTE)) {
            perfilUsuarioService.asignarPerfil(id, PERFIL_PACIENTE);
        }
    }

    /**
     * Marca como {@code REVOCADA} todas las relaciones médico-paciente vigentes del médico
     * indicado y notifica a cada paciente afectado. Compartido por la baja de la cuenta
     * ({@link #eliminarMedico(UUID)}) y la retirada del perfil médico
     * ({@link #revocarPerfilMedico(UUID, Usuario)}) para no duplicar la lógica.
     *
     * @param medicoId identificador del médico
     * @param medico   entidad del médico, para componer el nombre en la notificación
     * @param motivo   sufijo del mensaje enviado al paciente (p. ej. {@link #MENSAJE_MEDICO_NO_DISPONIBLE})
     */
    private void revocarRelacionesMedicoPaciente(UUID medicoId, Usuario medico, String motivo) {
        List<MedicoPaciente> relaciones = medicoPacienteRepository.findByMedicoIdAndEstadoNot(medicoId, MedicoPaciente.ESTADO_REVOCADA);
        if (relaciones == null || relaciones.isEmpty()) {
            return;
        }

        String nombreMedico = medico.getNombre() != null ? medico.getNombre() : medico.getNif();
        String mensaje = MENSAJE_MEDICO_PREFIJO + nombreMedico + motivo;

        for (MedicoPaciente relacion : relaciones) {
            relacion.setEstado(MedicoPaciente.ESTADO_REVOCADA);
            notificacionFacade.crearNotificacionParaUsuario(relacion.getPaciente().getNif(), mensaje);
            // Deja sin efecto las propuestas de cambio que el médico tuviera pendientes con ese paciente.
            propuestaCambioClinicoService.anularPendientes(medicoId, relacion.getPaciente().getId());
        }
        medicoPacienteRepository.saveAll(relaciones);
    }

    private Usuario getUsuarioFromId(UUID id) {
        if (id == null) {
            String error = ErrorMessages.campoRequerido(CAMPO_ID_USUARIO);
            log.error(error);
            throw new MedicoValidationException(error);
        }
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }
}
