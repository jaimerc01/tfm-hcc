package com.hcc.tfm_hcc.facade;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.hcc.tfm_hcc.dto.UsuarioDTO;

/**
 * Facade de administración para el sistema HCC.
 * Proporciona una interfaz unificada para las operaciones administrativas
 * relacionadas con la gestión de usuarios y perfiles médicos del sistema.
 * 
 * <p>Este facade encapsula:</p>
 * <ul>
 *   <li>Gestión completa de médicos (CRUD)</li>
 *   <li>Asignación y revocación de perfiles médicos</li>
 *   <li>Búsqueda de usuarios por identificadores</li>
 *   <li>Respuestas HTTP estandarizadas para operaciones administrativas</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AdminFacade {
    
    /**
     * Lista todos los usuarios con perfil de médico en el sistema.
     * 
     * @return ResponseEntity con la lista de UsuarioDTO que tienen perfil médico
     */
    ResponseEntity<List<UsuarioDTO>> listarMedicos();
    
    /**
     * Crea un nuevo usuario con perfil de médico en el sistema.
     * 
     * @param medicoDTO Datos del médico a crear
     * @return ResponseEntity con el UsuarioDTO del médico creado
     */
    ResponseEntity<UsuarioDTO> crearMedico(UsuarioDTO medicoDTO);
    
    /**
     * Actualiza la información de un médico existente.
     * 
     * @param id ID único del médico a actualizar
     * @param medicoDTO Nuevos datos del médico
     * @return ResponseEntity con el UsuarioDTO actualizado del médico
     */
    ResponseEntity<UsuarioDTO> actualizarMedico(UUID id, UsuarioDTO medicoDTO);
    
    /**
     * Elimina un médico del sistema.
     * 
     * @param id ID único del médico a eliminar
     * @return ResponseEntity vacío indicando el resultado de la operación
     */
    ResponseEntity<Void> eliminarMedico(UUID id);
    
    /**
     * Asigna o revoca el perfil de médico a un usuario existente.
     * 
     * @param id ID único del usuario al cual asignar o revocar el perfil médico
     * @param asignar true para asignar el perfil médico, false para revocarlo
     * @return ResponseEntity vacío indicando el resultado de la operación
     */
    ResponseEntity<Void> setPerfilMedico(UUID id, boolean asignar);
    
    /**
     * Busca un usuario específico por su NIF.
     * 
     * @param nif NIF (Número de Identificación Fiscal) del usuario a buscar
     * @return ResponseEntity con el UsuarioDTO del usuario encontrado
     */
    ResponseEntity<UsuarioDTO> buscarUsuarioPorNif(String nif);
}
