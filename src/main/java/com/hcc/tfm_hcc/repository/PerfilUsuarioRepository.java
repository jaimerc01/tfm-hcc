package com.hcc.tfm_hcc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.PerfilUsuario;

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
@Repository
public interface PerfilUsuarioRepository extends CrudRepository<PerfilUsuario, Long> {

    /**
     * Obtiene todos los perfiles asignados a un usuario identificado por su NIF.
     * Permite conocer todos los roles y permisos que tiene un usuario en el sistema.
     * 
     * @param nif NIF (Número de Identificación Fiscal) del usuario
     * @return Lista de Perfil asignados al usuario con el NIF especificado
     */
    @Query("SELECT p.perfil FROM PerfilUsuario p WHERE p.usuario.nif = :nif")
    List<Perfil> getPerfilesByNif(@Param("nif") String nif);
}
