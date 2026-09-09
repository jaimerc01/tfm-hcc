package com.hcc.tfm_hcc.facade.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.AdminOperacionException;
import com.hcc.tfm_hcc.exception.AdminValidationException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.facade.AdminFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del facade de administración para el sistema HCC.
 * Proporciona operaciones administrativas centralizadas para la gestión
 * de usuarios médicos y operaciones de administración del sistema.
 * 
 * <p>Esta implementación gestiona:</p>
 * <ul>
 *   <li>Operaciones CRUD completas para médicos del sistema</li>
 *   <li>Asignación y revocación dinámica de perfiles médicos</li>
 *   <li>Búsqueda optimizada de usuarios por identificadores</li>
 *   <li>Logging estructurado para auditoría administrativa</li>
 * </ul>
 *
 * <p>Devuelve DTOs y modelos de dominio (no {@code ResponseEntity}): traducir el
 * resultado a una respuesta HTTP con su código de estado es responsabilidad del
 * controlador.</p>
 * 
 * <p>Características de seguridad y validación:</p>
 * <ul>
 *   <li>Validación exhaustiva de datos de entrada</li>
 *   <li>Manejo robusto de errores con logging detallado</li>
 *   <li>Prevención de operaciones inconsistentes</li>
 *   <li>Mensajes de error centralizados y consistentes</li>
 * </ul>
 * 
 * <p>La clase implementa el patrón de inyección de dependencias mediante constructor
 * y utiliza logging estructurado para facilitar la auditoría de operaciones administrativas.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 * @see AdminFacade
 * @see MedicoService
 * @see UsuarioRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFacadeImpl implements AdminFacade {

    /** Longitud mínima de un NIF/NIE español (8 dígitos + letra). */
    private static final int LONGITUD_MINIMA_NIF = 9;

    /**
     * Servicio de gestión de médicos que encapsula la lógica de negocio
     * para operaciones CRUD y administrativas sobre usuarios médicos.
     */
    private final MedicoService medicoService;

    /**
     * Repositorio de acceso a datos de usuarios del sistema.
     * Proporciona operaciones de consulta y manipulación de entidades Usuario.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Mapper para transformación entre entidades Usuario y UsuarioDTO.
     * Facilita la conversión de datos entre capas del sistema.
     */
    private final UsuarioMapper usuarioMapper;

    /**
     * Servicio para calcular el índice de búsqueda (HMAC) del NIF, necesario porque
     * el NIF está cifrado de forma no determinista y no es consultable por igualdad.
     */
    private final HmacSearchIndexService hmacSearchIndexService;

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que obtiene la lista completa de usuarios con perfil médico,
     * proporcionando información detallada para operaciones administrativas.</p>
     * 
     * @return lista de UsuarioDTO de todos los médicos del sistema
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioDTO> listarMedicos() {
        log.debug("Solicitando lista completa de médicos del sistema");

        try {
            List<UsuarioDTO> medicos = medicoService.listarMedicos();
            log.info("Se obtuvieron {} médicos del sistema", medicos.size());
            return medicos;
        } catch (Exception e) {
            log.error("Error al obtener lista de médicos: {}", e.getMessage(), e);
            throw new AdminOperacionException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que crea un nuevo usuario médico en el sistema,
     * validando los datos de entrada y asignando automáticamente el perfil médico.</p>
     * 
     * @param medicoDTO Datos del médico a crear en el sistema
     * @return el UsuarioDTO del médico creado exitosamente
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDTO crearMedico(UsuarioDTO medicoDTO) {
        log.debug("Creando nuevo médico en el sistema");

        try {
            validarDatosMedico(medicoDTO);

            UsuarioDTO medicoCreado = medicoService.crearMedico(medicoDTO);
            log.info("Médico creado exitosamente con ID: {}", medicoCreado.getId());
            return medicoCreado;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al crear médico: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear médico: {}", e.getMessage(), e);
            throw new AdminOperacionException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que actualiza los datos de un médico existente,
     * validando tanto la existencia del médico como la integridad de los nuevos datos.</p>
     * 
     * @param id Identificador único del médico a actualizar
     * @param medicoDTO Nuevos datos del médico
     * @return el UsuarioDTO actualizado del médico
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO) {
        log.debug("Actualizando datos de médico con ID: {}", id);

        try {
            validarIdMedico(id);
            validarDatosMedico(medicoDTO);

            UsuarioDTO medicoActualizado = medicoService.actualizarMedico(id, medicoDTO);
            log.info("Médico actualizado exitosamente: ID {}", id);
            return medicoActualizado;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al actualizar médico con ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar médico con ID {}: {}", id, e.getMessage(), e);
            throw new AdminOperacionException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que elimina un médico del sistema de forma segura,
     * validando la existencia del médico y gestionando las dependencias apropiadamente.</p>
     * 
     * @param id Identificador único del médico a eliminar del sistema
     * @return el ID del médico eliminado, para confirmación
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UUID eliminarMedico(UUID id) {
        log.debug("Solicitando eliminación de médico con ID: {}", id);

        try {
            validarIdMedico(id);

            UUID idEliminado = medicoService.eliminarMedico(id);
            log.info("Médico eliminado exitosamente: ID {}", idEliminado);
            return idEliminado;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al eliminar médico con ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar médico con ID {}: {}", id, e.getMessage(), e);
            throw new AdminOperacionException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que asigna o revoca el perfil médico de un usuario,
     * validando la existencia del usuario y el estado actual del perfil.</p>
     * 
     * @param id Identificador único del usuario para modificar su perfil
     * @param asignar true para asignar perfil médico, false para revocarlo
     * @return el ID del usuario afectado, para confirmación
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UUID setPerfilMedico(UUID id, boolean asignar) {
        log.debug("Modificando perfil médico para usuario ID: {} - Asignar: {}", id, asignar);

        try {
            validarIdUsuario(id);

            medicoService.setPerfilMedico(id, asignar);
            String accion = asignar ? "asignado" : "revocado";
            log.info("Perfil médico {} exitosamente para usuario ID: {}", accion, id);
            return id;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al modificar perfil médico para usuario ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al modificar perfil médico para usuario ID {}: {}", id, e.getMessage(), e);
            throw new AdminOperacionException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que busca un usuario específico por su NIF,
     * validando el formato del identificador y manejando casos de usuario no encontrado.</p>
     * 
     * @param nif NIF del usuario a buscar en el sistema
     * @return el UsuarioDTO del usuario encontrado
     * @throws UsuarioNoEncontradoException si no existe ningún usuario con ese NIF
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDTO buscarUsuarioPorNif(String nif) {
        String nifLog = LogMaskUtil.enmascarar(nif);
        log.debug("Buscando usuario por NIF: {}", nifLog);

        try {
            validarNif(nif);

            Optional<Usuario> usuarioOpt = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nif));
            Usuario usuario = usuarioOpt.orElseThrow(() -> {
                log.warn("Usuario no encontrado con NIF: {}", nifLog);
                // El mensaje se registra tal cual más abajo, así que lleva el NIF enmascarado.
                return new UsuarioNoEncontradoException(ErrorMessages.entidadNoEncontrada("Usuario", nifLog));
            });

            UsuarioDTO usuarioDTO = usuarioMapper.toDto(usuario);
            log.info("Usuario encontrado exitosamente por NIF: {}", nifLog);
            return usuarioDTO;
        } catch (AdminValidationException | UsuarioNoEncontradoException e) {
            log.warn("No se pudo obtener el usuario por NIF {}: {}", nifLog, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al buscar usuario por NIF {}: {}", nifLog, e.getMessage(), e);
            throw new AdminOperacionException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida que los datos del médico sean correctos y completos.
     * 
     * @param medicoDTO Datos del médico a validar
     * @throws AdminValidationException si los datos son inválidos
     */
    private void validarDatosMedico(UsuarioDTO medicoDTO) {
        if (medicoDTO == null) {
            throw new AdminValidationException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }
        
        if (medicoDTO.getNif() == null || medicoDTO.getNif().trim().isEmpty()) {
            throw new AdminValidationException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (medicoDTO.getNombre() == null || medicoDTO.getNombre().trim().isEmpty()) {
            throw new AdminValidationException(ErrorMessages.campoRequerido("nombre"));
        }
        
        if (medicoDTO.getApellido1() == null || medicoDTO.getApellido1().trim().isEmpty()) {
            throw new AdminValidationException(ErrorMessages.campoRequerido("primer apellido"));
        }
        
        if (medicoDTO.getEmail() == null || medicoDTO.getEmail().trim().isEmpty()) {
            throw new AdminValidationException(ErrorMessages.campoRequerido("email"));
        }
    }

    /**
     * Valida que el ID del médico sea válido.
     * 
     * @param id ID del médico a validar
     * @throws AdminValidationException si el ID es inválido
     */
    private void validarIdMedico(UUID id) {
        if (id == null) {
            throw new AdminValidationException(ErrorMessages.campoRequerido("ID del médico"));
        }
    }

    /**
     * Valida que el ID del usuario sea válido.
     * 
     * @param id ID del usuario a validar
     * @throws AdminValidationException si el ID es inválido
     */
    private void validarIdUsuario(UUID id) {
        if (id == null) {
            throw new AdminValidationException(ErrorMessages.campoRequerido("ID del usuario"));
        }
    }

    /**
     * Valida que el NIF sea válido.
     * 
     * @param nif NIF a validar
     * @throws AdminValidationException si el NIF es inválido
     */
    private void validarNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            throw new AdminValidationException(ErrorMessages.campoRequerido("NIF"));
        }

        if (nif.trim().length() < LONGITUD_MINIMA_NIF) {
            throw new AdminValidationException(ErrorMessages.ERROR_DNI_INVALIDO);
        }
    }
}
