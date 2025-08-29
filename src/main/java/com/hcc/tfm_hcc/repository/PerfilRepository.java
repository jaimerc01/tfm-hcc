package com.hcc.tfm_hcc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Perfil;

/**
 * Repositorio para la gestión de perfiles de usuario en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para los diferentes
 * roles y perfiles disponibles en el sistema.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Buscar perfiles por rol específico (case-insensitive)</li>
 *   <li>Gestionar los diferentes tipos de usuario del sistema</li>
 *   <li>Mantener la consistencia de roles y permisos</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface PerfilRepository extends CrudRepository<Perfil, Long> {

    /**
     * Busca un perfil por su rol específico.
     * La búsqueda se realiza de manera case-insensitive para mayor flexibilidad.
     * 
     * @param rol Nombre del rol a buscar (ej: "MEDICO", "PACIENTE", "ADMIN")
     * @return Optional con el Perfil correspondiente al rol si existe, empty() en caso contrario
     */
    @Query("SELECT p FROM Perfil p WHERE LOWER(p.rol) = LOWER(:rol)")
    Optional<Perfil> getPerfilByRol(@Param("rol") String rol);
}
