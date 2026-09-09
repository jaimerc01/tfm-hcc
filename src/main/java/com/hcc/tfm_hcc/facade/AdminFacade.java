package com.hcc.tfm_hcc.facade;

import java.util.List;
import java.util.UUID;

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
 * </ul>
 *
 * <p>Devuelve DTOs y modelos de dominio, no {@code ResponseEntity}: la traducción a
 * respuesta HTTP (código de estado incluido) es responsabilidad exclusiva del
 * controlador.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AdminFacade {

    /**
     * Lista todos los usuarios con perfil de médico en el sistema.
     *
     * @return lista de UsuarioDTO que tienen perfil médico
     */
    List<UsuarioDTO> listarMedicos();

    /**
     * Crea un nuevo usuario con perfil de médico en el sistema.
     *
     * @param medicoDTO Datos del médico a crear
     * @return el UsuarioDTO del médico creado
     */
    UsuarioDTO crearMedico(UsuarioDTO medicoDTO);

    /**
     * Actualiza la información de un médico existente.
     *
     * @param id ID único del médico a actualizar
     * @param medicoDTO Nuevos datos del médico
     * @return el UsuarioDTO actualizado del médico
     */
    UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO);

    /**
     * Elimina un médico del sistema.
     *
     * @param id ID único del médico a eliminar
     * @return el ID del médico eliminado, para confirmación
     */
    UUID eliminarMedico(UUID id);

    /**
     * Asigna o revoca el perfil de médico a un usuario existente.
     *
     * @param id ID único del usuario al cual asignar o revocar el perfil médico
     * @param asignar true para asignar el perfil médico, false para revocarlo
     * @return el ID del usuario afectado, para confirmación
     */
    UUID setPerfilMedico(UUID id, boolean asignar);

    /**
     * Busca un usuario específico por su NIF.
     *
     * @param nif NIF (Número de Identificación Fiscal) del usuario a buscar
     * @return el UsuarioDTO del usuario encontrado
     * @throws com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException si no existe ningún usuario con ese NIF
     */
    UsuarioDTO buscarUsuarioPorNif(String nif);
}
