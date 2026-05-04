package com.hcc.tfm_hcc.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Usuario;

/**
 * Repositorio para la gestión de usuarios en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para los usuarios
 * del sistema, incluyendo validaciones de unicidad y búsquedas específicas.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Buscar usuarios por NIF y email de manera eficiente</li>
 *   <li>Validar unicidad de campos críticos durante actualizaciones</li>
 *   <li>Mantener la integridad de datos de identificación única</li>
 *   <li>Proporcionar acceso seguro a información de usuarios</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, UUID> {

    /**
     * Busca un usuario por su NIF (Número de Identificación Fiscal).
     * El NIF es un identificador único en el sistema para cada usuario.
     * 
     * @param nif NIF del usuario a buscar
     * @return Optional con el Usuario si existe, empty() en caso contrario
     */
    Optional<Usuario> findByNif(String nif);

    /**
     * Busca un usuario por su dirección de email.
     * El email es un identificador único alternativo en el sistema.
     * 
     * @param email Dirección de email del usuario a buscar
     * @return Optional con el Usuario si existe, empty() en caso contrario
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe otro usuario con el mismo NIF, excluyendo un ID específico.
     * Útil para validar unicidad durante actualizaciones de perfil de usuario.
     * 
     * @param nif NIF a verificar
     * @param id ID del usuario a excluir de la verificación
     * @return true si existe otro usuario con el mismo NIF, false en caso contrario
     */
    boolean existsByNifAndIdNot(String nif, UUID id);
    
    /**
     * Verifica si existe otro usuario con el mismo email, excluyendo un ID específico.
     * Útil para validar unicidad durante actualizaciones de perfil de usuario.
     * 
     * @param email Email a verificar
     * @param id ID del usuario a excluir de la verificación
     * @return true si existe otro usuario con el mismo email, false en caso contrario
     */
    boolean existsByEmailAndIdNot(String email, UUID id);
}
