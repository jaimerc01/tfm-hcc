package com.hcc.tfm_hcc.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.AccessLog;

/**
 * Repositorio para la gestión de registros de acceso (logs) en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para el historial de accesos de usuarios.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Consultar el historial completo de accesos de un usuario</li>
 *   <li>Filtrar accesos por rangos de fechas para auditorías</li>
 *   <li>Mantener trazabilidad de todas las actividades del usuario</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface AccessLogRepository extends CrudRepository<AccessLog, UUID> {
    
    /**
     * Busca todos los registros de acceso de un usuario específico.
     * Los resultados se ordenan por timestamp de forma descendente (más recientes primero).
     * 
     * @param usuarioId ID único del usuario del cual obtener los logs de acceso
     * @return Lista de AccessLog del usuario ordenados por timestamp descendente
     */
    List<AccessLog> findByUsuarioIdOrderByTimestampDesc(String usuarioId);
    
    /**
     * Busca los registros de acceso de un usuario en un rango de fechas específico.
     * Útil para auditorías y análisis de actividad en períodos determinados.
     * Los resultados se ordenan por timestamp de forma descendente.
     * 
     * @param usuarioId ID único del usuario del cual obtener los logs de acceso
     * @param desde Fecha y hora de inicio del rango de búsqueda (inclusive)
     * @param hasta Fecha y hora de fin del rango de búsqueda (inclusive)
     * @return Lista de AccessLog del usuario en el rango especificado, ordenados por timestamp descendente
     */
    List<AccessLog> findByUsuarioIdAndTimestampBetweenOrderByTimestampDesc(
        String usuarioId, 
        LocalDateTime desde, 
        LocalDateTime hasta
    );
}
