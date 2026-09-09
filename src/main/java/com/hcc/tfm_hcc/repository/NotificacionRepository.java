package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Repositorio para la gestión de notificaciones en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para las notificaciones
 * dirigidas a usuarios específicos del sistema.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Consultar notificaciones por usuario con ordenamiento cronológico</li>
 *   <li>Implementar paginación para grandes volúmenes de notificaciones</li>
 *   <li>Contar notificaciones no leídas para indicadores visuales</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    
    /**
     * Busca las notificaciones no eliminadas de un usuario, ordenadas por fecha de creación
     * descendente (más recientes primero).
     *
     * @param usuario Usuario del cual obtener las notificaciones
     * @return Lista de Notificacion (sin las eliminadas) ordenadas por fecha de creación descendente
     */
    List<Notificacion> findByUsuarioAndEliminadaFalseOrderByFechaCreacionDesc(Usuario usuario);

    /**
     * Busca las notificaciones no eliminadas de un usuario con soporte para paginación.
     *
     * @param usuario Usuario del cual obtener las notificaciones
     * @param pageable Configuración de paginación y ordenamiento
     * @return Page con las notificaciones del usuario (sin las eliminadas)
     */
    Page<Notificacion> findByUsuarioAndEliminadaFalse(Usuario usuario, Pageable pageable);

    /**
     * Cuenta las notificaciones no leídas (y no eliminadas) de un usuario, para el indicador
     * de notificaciones pendientes.
     *
     * @param usuario Usuario del cual contar las notificaciones
     * @return Número de notificaciones no leídas y no eliminadas del usuario
     */
    long countByUsuarioAndLeidaFalseAndEliminadaFalse(Usuario usuario);
}
