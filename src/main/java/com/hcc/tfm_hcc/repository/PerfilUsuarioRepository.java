package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Repositorio para la gestión de asociaciones entre usuarios y perfiles en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para las relaciones
 * muchos-a-muchos entre usuarios y sus perfiles asignados.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Consultar todos los perfiles asignados a un usuario específico</li>
 *   <li>Gestionar las asociaciones dinámicas de roles por usuario</li>
 *   <li>Mantener la integridad de permisos y autorizaciones</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface PerfilUsuarioRepository extends CrudRepository<PerfilUsuario, UUID> {

    /**
     * Obtiene todos los perfiles asignados a un usuario identificado por su NIF.
     * Permite conocer todos los roles y permisos que tiene un usuario en el sistema.
     *
     * <p>El NIF del usuario está cifrado de forma no determinista, así que el filtro se
     * aplica sobre {@code usuario.nifHash}, no sobre el NIF cifrado
     * (ver {@link com.hcc.tfm_hcc.service.HmacSearchIndexService}).</p>
     *
     * @param nifHash índice de búsqueda (HMAC) del NIF del usuario
     * @return Lista de Perfil asignados al usuario con el NIF especificado
     */
    @Query("SELECT p.perfil FROM PerfilUsuario p WHERE p.usuario.nifHash = :nifHash")
    List<Perfil> getPerfilesByNifHash(@Param("nifHash") String nifHash);

    /**
     * Busca la relación usuario-perfil para un usuario y rol concretos.
     *
     * @param usuarioId ID del usuario
     * @param rol rol a consultar
     * @return relación existente, si la hay
     */
    @Query("SELECT p FROM PerfilUsuario p WHERE p.usuario.id = :usuarioId AND UPPER(p.perfil.rol) = UPPER(:rol)")
    Optional<PerfilUsuario> findByUsuarioIdAndPerfilRol(@Param("usuarioId") UUID usuarioId, @Param("rol") String rol);

    /**
     * Comprueba si un usuario tiene asignado un rol concreto.
     *
     * @param usuarioId ID del usuario
     * @param rol rol a comprobar
     * @return true si existe la relación
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PerfilUsuario p WHERE p.usuario.id = :usuarioId AND UPPER(p.perfil.rol) = UPPER(:rol)")
    boolean existsByUsuarioIdAndPerfilRol(@Param("usuarioId") UUID usuarioId, @Param("rol") String rol);

    /**
     * Devuelve los usuarios que tienen asignado un rol concreto.
     *
     * @param rol rol a filtrar
     * @return usuarios con ese rol
     */
    @Query("SELECT DISTINCT p.usuario FROM PerfilUsuario p WHERE UPPER(p.perfil.rol) = UPPER(:rol)")
    List<Usuario> findUsuariosByPerfilRol(@Param("rol") String rol);
}
