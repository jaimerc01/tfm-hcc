package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.ArchivoClinico;

/**
 * Repositorio para la gestión de archivos clínicos en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para archivos médicos
 * asociados a usuarios específicos.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Consultar archivos clínicos por usuario con ordenamiento cronológico</li>
 *   <li>Buscar archivos específicos validando propiedad del usuario</li>
 *   <li>Mantener la integridad de datos entre archivos y usuarios</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ArchivoClinicoRepository extends JpaRepository<ArchivoClinico, UUID> {
    
    /**
     * Busca todos los archivos clínicos de un usuario específico.
     * Los resultados se ordenan por fecha de creación de forma descendente (más recientes primero).
     * 
     * @param usuarioId ID único del usuario propietario de los archivos
     * @return Lista de ArchivoClinico del usuario ordenados por fecha de creación descendente
     */
    List<ArchivoClinico> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);
    
    /**
     * Busca un archivo clínico específico validando que pertenezca al usuario indicado.
     * Esta consulta proporciona seguridad adicional al verificar la propiedad del archivo.
     * 
     * @param id ID único del archivo clínico a buscar
     * @param usuarioId ID único del usuario que debe ser propietario del archivo
     * @return Optional con el ArchivoClinico si existe y pertenece al usuario, empty() en caso contrario
     */
    Optional<ArchivoClinico> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
