package com.hcc.tfm_hcc.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

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
public interface UsuarioRepository extends CrudRepository<Usuario, UUID> {

    /**
     * Busca un usuario por el índice de búsqueda de su NIF (Número de Identificación Fiscal).
     * El NIF en sí está cifrado de forma no determinista, así que la búsqueda por igualdad
     * se hace contra {@code nifHash} (ver {@link com.hcc.tfm_hcc.service.HmacSearchIndexService}),
     * no contra el NIF cifrado. El NIF es un identificador único en el sistema para cada usuario.
     *
     * @param nifHash índice de búsqueda (HMAC) del NIF del usuario a buscar
     * @return Optional con el Usuario si existe, empty() en caso contrario
     */
    Optional<Usuario> findByNifHash(String nifHash);

    /**
     * Busca un usuario por el índice de búsqueda de su dirección de email.
     * El email en sí está cifrado de forma no determinista, así que la búsqueda por igualdad
     * se hace contra {@code emailHash}, no contra el email cifrado. El email es un
     * identificador único alternativo en el sistema.
     *
     * @param emailHash índice de búsqueda (HMAC) del email del usuario a buscar
     * @return Optional con el Usuario si existe, empty() en caso contrario
     */
    Optional<Usuario> findByEmailHash(String emailHash);

    /**
     * Verifica si existe otro usuario con el mismo NIF, excluyendo un ID específico.
     * Útil para validar unicidad durante actualizaciones de perfil de usuario.
     *
     * @param nifHash índice de búsqueda (HMAC) del NIF a verificar
     * @param id ID del usuario a excluir de la verificación
     * @return true si existe otro usuario con el mismo NIF, false en caso contrario
     */
    boolean existsByNifHashAndIdNot(String nifHash, UUID id);

    /**
     * Verifica si existe otro usuario con el mismo email, excluyendo un ID específico.
     * Útil para validar unicidad durante actualizaciones de perfil de usuario.
     *
     * @param emailHash índice de búsqueda (HMAC) del email a verificar
     * @param id ID del usuario a excluir de la verificación
     * @return true si existe otro usuario con el mismo email, false en caso contrario
     */
    boolean existsByEmailHashAndIdNot(String emailHash, UUID id);
}
